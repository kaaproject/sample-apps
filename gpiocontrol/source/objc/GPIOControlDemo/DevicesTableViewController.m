/**
 *  Copyright 2014-2016 CyberVision, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#import "DevicesTableViewController.h"
#import "KaaClientManager.h"
#import "Device.h"
#import "ConnectionAlert.h"
#import "GPIOTableViewController.h"

@import Kaa;

#define TAG @"DevicesTableViewController"

@interface DevicesTableViewController () <RemoteControlECFDelegate, OnDetachEndpointOperationDelegate, OnAttachEndpointOperationDelegate>

@property (nonatomic, strong) NSMutableArray *devices;
@property (nonatomic, strong) NSIndexPath *deletingIndexPath;

@end

@implementation DevicesTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //Checking connection...
    ConnectivityChecker *checker = [[ConnectivityChecker alloc] init];
    if ([checker isConnected]) {
        self.devices = [NSMutableArray array];
        [[KaaClientManager sharedManager] sendDeviceInfoRequestToAllWithDelegate:self];
    } else {
        [self presentViewController:[ConnectionAlert noConnectionAlert]
                           animated:YES
                         completion:nil];
    }
}

#pragma mark - Actions

- (IBAction)plusButtonPressed:(UIBarButtonItem *)sender {
    UIAlertController *alert = nil;
    if ([[[ConnectivityChecker alloc] init] isConnected]) {
        alert = [UIAlertController alertControllerWithTitle:@"Access token"
                                                     message:@"Enter access token"
                                              preferredStyle:UIAlertControllerStyleAlert];
        
        [alert addTextFieldWithConfigurationHandler:^(UITextField *textField) {
            textField.placeholder = @"Access token";
        }];
        
        UIAlertAction *ok = [UIAlertAction actionWithTitle:@"OK"
                                                     style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction * action) {
                                                       [self handleEndpointIdInput:alert.textFields.lastObject.text];
                                                   }];
        UIAlertAction *cancel = [UIAlertAction actionWithTitle:@"Cancel"
                                                         style:UIAlertActionStyleCancel
                                                       handler:nil];
        [alert addAction:ok];
        [alert addAction:cancel];
        
    } else {
        alert = [ConnectionAlert noConnectionAlert];
    }
    [self presentViewController:alert animated:YES completion:nil];
}

- (IBAction)refreshButtonPressed:(UIBarButtonItem *)sender {
    [self.tableView reloadData];
}

#pragma mark - UITableViewDatasource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.devices.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"DeviceCell" forIndexPath:indexPath];
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
        Device *currentDevice = self.devices[indexPath.row];
        self.deletingIndexPath = indexPath;
        [[KaaClientManager sharedManager] detachEndpoint:currentDevice.kaaEndpointId
                                                delegate:self];
    }
}

#pragma mark - RemoteControlECFDelegate

- (void)onRemoteControlECFDeviceInfoResponse:(RemoteControlECFDeviceInfoResponse *)event fromSource:(NSString *)source {
    NSLog(@"%@ Got DeviceInfoResponse", TAG);
    Device *device = [[Device alloc] initWithModel:event.model
                                        deviceName:event.deviceName
                                     kaaEndpointId:source
                                      GPIOStatuses:event.gpioStatus];
    [self addItem:device];
}

#pragma mark - OnDetachEndpointOperationDelegate

- (void)onDetachResult:(SyncResponseResultType)result {
    NSLog(@"%@ Detach result: %u", TAG, result);
    if (result == SYNC_RESPONSE_RESULT_TYPE_SUCCESS) {
        [self.devices removeObjectAtIndex:self.deletingIndexPath.row];
        [self reloadTable];
    }
}

#pragma mark - OnAttachEndpointOperationDelegate

- (void)onAttachResult:(SyncResponseResultType)result withEndpointKeyHash:(EndpointKeyHash *)endpointKeyHash {
    NSLog(@"%@ attachEndpoint result: %u", TAG, result);
}

#pragma mark - Helper methods

- (void)handleEndpointIdInput:(NSString *)text
{
    if ([text isEqualToString:@""]) {
        [self presentViewController:[ConnectionAlert emtpyEndpointIdAlert] animated:YES completion:nil];
    } else {
        NSString *endpointId = text;
        [[KaaClientManager sharedManager] attachEndpoint:endpointId delegate:self];
        [[KaaClientManager sharedManager] sendDeviceInfoRequestToAllWithDelegate:self];
    }
}

- (void)addItem:(Device *)device {
    if ([self.devices containsObject:device]) {
        NSInteger index = [self.devices indexOfObject:device];
        [self.devices replaceObjectAtIndex:index withObject:device];
    } else {
        [self.devices addObject:device];
    }
    [self reloadTable];
}

- (void)reloadTable {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.tableView reloadData];
    });
}

#pragma mark - Navigation

- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    if ([segue.identifier isEqualToString:@"showStatuses"] &&
        [sender isKindOfClass:[UITableViewCell class]]) {
        
        NSIndexPath *indexPath = [self.tableView indexPathForCell:sender];
        GPIOTableViewController *controller = (GPIOTableViewController *)segue.destinationViewController;
        controller.device = self.devices[indexPath.row];
    }
}

@end
