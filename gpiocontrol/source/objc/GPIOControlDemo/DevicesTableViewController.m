/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "DevicesTableViewController.h"
#import "KaaProvider.h"
#import "PreferencesManager.h"
#import "Device.h"
#import "ConnectionAlert.h"
#import "GPIOTableViewController.h"

#define TAG @"DevicesTableViewController"

@interface DevicesTableViewController ()

@property (nonatomic, strong) id <KaaClient> kaaClient;
@property (nonatomic, strong) KAAEmptyData *profile;
@property (nonatomic, strong) NSMutableArray *devices;
@property (nonatomic, strong) NSString *endpointId;
@property (nonatomic, strong) NSIndexPath *deletingIndexPath;

@end

@implementation DevicesTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //Starting Kaa client
    [self startKaa];
}

- (void)startKaa {
    //Checking connection...
    ConnectivityChecker *checker = [[ConnectivityChecker alloc] init];
    if ([checker isConnected]) {
        //Get Kaa client instance from KaaProvider class
        self.kaaClient = [KaaProvider getClient];
        [self.kaaClient setProfileContainer:self];
        self.profile = [[KAAEmptyData alloc] init];
        self.devices = [NSMutableArray array];
        //Starting Kaa client
        [self.kaaClient start];
        //Attaching new user at first application opening
        if ([self isFirstTimeOpening]) {
            [self initKaa];
        }
        [self setUpEndpointDelegate];
    } else {
        [self presentViewController:[ConnectionAlert showAlertNoConnection] animated:YES completion:nil];
    }
}

- (void)initKaa {
    [PreferencesManager setUserExternalId:@"2"];
    [PreferencesManager setUserAccessToken:@"iOSToken"];
    NSLog(@"%@ Attaching user...", TAG);
    [[[KaaProvider alloc] init] attachUser];
}

- (void)setUpEndpointDelegate {
    ConnectivityChecker *checker = [[ConnectivityChecker alloc] init];
    if ([checker isConnected]) {
        if (!self.kaaClient) {
            [self startKaa];
        }
        [KaaProvider setUpEventDelegate:self];
    } else {
        [self presentViewController:[ConnectionAlert showAlertNoConnection] animated:YES completion:nil];
    }
}

- (void)addItem:(Device *)device {
    if ([self.devices containsObject:device]) {
        NSInteger index = [self.devices indexOfObject:device];
        [self.devices replaceObjectAtIndex:index withObject:device];
        [self reloadTable];
    } else {
        [self.devices addObject:device];
        [self reloadTable];
    }
}

- (BOOL)isFirstTimeOpening {
    NSUserDefaults *theDefaults = [NSUserDefaults standardUserDefaults];
    if([theDefaults integerForKey:@"hasRun"] == 0) {
        return true;
    }
    return false;
}

- (void)reloadTable {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.tableView reloadData];
    });
}

- (KAAEmptyData *)getProfile {
    return self.profile;
}

#pragma mark - Actions

- (IBAction)plusButtonPressed:(UIBarButtonItem *)sender {
    if ([[[ConnectivityChecker alloc] init] isConnected]) {
        UIAlertController *alert = [UIAlertController
                                    alertControllerWithTitle:@"Access token"
                                    message:@"Enter access token"
                                    preferredStyle:UIAlertControllerStyleAlert];
        [alert addTextFieldWithConfigurationHandler:^(UITextField *textField) {
            textField.placeholder = @"Access token";
        }];
        UIAlertAction *ok = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction * action) {
                                                       if ([alert.textFields.lastObject.text isEqualToString:@""]) {
                                                           [self presentViewController:[ConnectionAlert showAlertEmtpyEndpointId] animated:YES completion:nil];
                                                       } else {
                                                           self.endpointId = alert.textFields.lastObject.text;
                                                           [self.kaaClient attachEndpointWithAccessToken:[[EndpointAccessToken alloc] initWithToken:self.endpointId] delegate:self];
                                                           [KaaProvider sendDeviceInfoRequestToAll];
                                                       }
                                                   }];
        UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"Cancel" style:UIAlertActionStyleDefault
                                                       handler:^(UIAlertAction * action) {
                                                           [alert dismissViewControllerAnimated:YES completion:nil];
                                                       }];
        [alert addAction:ok];
        [alert addAction:cancel];
        
        [self presentViewController:alert animated:YES completion:nil];
    } else {
        [self presentViewController:[ConnectionAlert showAlertNoConnection] animated:YES completion:nil];
    }
    
}

- (IBAction)refreshButtonPressed:(UIBarButtonItem *)sender {
    [self.tableView reloadData];
}

#pragma mark - Table view data source and delegate

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.devices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"identifier" forIndexPath:indexPath];
    Device *device = self.devices[indexPath.row];
    cell.textLabel.text = [NSString stringWithFormat:@"%@, %luGPIO", device.model, (unsigned long)device.gpioStatuses.count];
    cell.detailTextLabel.text = device.deviceName;
    return cell;
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        id <KaaClient> kaaClient = [KaaProvider getClient];
        NSLog(@"%@ Going to detach.", TAG);
        Device *currentDevice = self.devices[indexPath.row];
        self.deletingIndexPath = indexPath;
        [kaaClient detachEndpointWithKeyHash:[[EndpointKeyHash alloc] initWithKeyHash:currentDevice.kaaEndpointId] delegate:self];
    }
}

#pragma mark - Delegates methods

- (void)onDeviceInfoResponse:(DeviceInfoResponse *)event fromSource:(NSString *)source {
    NSLog(@"%@ Got DeviceInfoResponse", TAG);
    Device *device = [[Device alloc] initWithModel:event.model deviceName:event.deviceName kaaEndpointId:source GPIOStatuses:event.gpioStatus];
    [self addItem:device];
}

- (void)onDetachResult:(SyncResponseResultType)result {
    NSLog(@"%@ SyncresponseResultType: %u", TAG, result);
    if (result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
        [self.devices removeObjectAtIndex:self.deletingIndexPath.row];
        [self reloadTable];
    }
}

- (void)onAttachResult:(SyncResponseResultType)result withEndpointKeyHash:(EndpointKeyHash *)endpointKeyHash {
    NSLog(@"%@ attachEndpoint result: %u", TAG, result);
}

#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"showStatuses"] && [sender isKindOfClass:[UITableViewCell class]]) {
        NSIndexPath *indexPath = [self.tableView indexPathForCell:sender];
        GPIOTableViewController *controller = (GPIOTableViewController *)segue.destinationViewController;
        controller.device = self.devices[indexPath.row];
    }
}

@end
