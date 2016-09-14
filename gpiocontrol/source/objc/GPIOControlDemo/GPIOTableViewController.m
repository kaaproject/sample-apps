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

#import "GPIOTableViewController.h"
#import "StatusTableViewCell.h"
#import "KaaClientManager.h"
#import "ConnectionAlert.h"
#import "Device.h"

@import Kaa;

@interface GPIOTableViewController ()

@end

@implementation GPIOTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.gpioStatusArray = [self.device getGpioStatuses];
    [self sortGpioStatuses];
    ConnectivityChecker *checker = [[ConnectivityChecker alloc]init];
    if (![checker isConnected]) {
        [self presentViewController:[ConnectionAlert noConnectionAlert] animated:YES completion:nil];
    }
}

- (void)sortGpioStatuses {
    NSArray *sortedArray = [self.gpioStatusArray sortedArrayUsingComparator:^ NSComparisonResult(RemoteControlECFGpioStatus *obj1, RemoteControlECFGpioStatus *obj2) {
        return [@(obj1.id) compare:@(obj1.id)];
    }];
    self.gpioStatusArray = sortedArray;
}

#pragma mark - Table view data source

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.gpioStatusArray.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *cellIdentifier = @"cell";
    StatusTableViewCell *cell = (StatusTableViewCell *)[tableView dequeueReusableCellWithIdentifier:cellIdentifier forIndexPath:indexPath];
    RemoteControlECFGpioStatus *status = self.gpioStatusArray[indexPath.row];
    cell.idLabel.text = [NSString stringWithFormat:@"GPIO id: %d", status.id];
    cell.statusSwitch.tag = indexPath.row;
    [cell.statusSwitch setOn:status.status animated:YES];
    return cell;
}

#pragma mark - Actions

- (IBAction)statusSwitchChanged:(UISwitch *)sender {
    ConnectivityChecker *checker = [[ConnectivityChecker alloc] init];
    if ([checker isConnected]) {
        [self sendToggleRequest:sender];
    } else {
        [self presentViewController:[ConnectionAlert noConnectionAlert]
                           animated:YES
                         completion:nil];
    }
}

- (void)sendToggleRequest:(UISwitch *)sender {
    RemoteControlECFGpioStatus *status = [self.gpioStatusArray objectAtIndex:sender.tag];
    status.status = sender.on;
    id<KaaClient> client = [KaaClientManager sharedManager].kaaClient;
    EventFamilyFactory *eventFamilyFactory = [client getEventFamilyFactory];
    RemoteControlECF *ecf = [eventFamilyFactory getRemoteControlECF];
    RemoteControlECFGpioToggleRequest *request = [[RemoteControlECFGpioToggleRequest alloc] init];
    request.gpio = self.gpioStatusArray[sender.tag];
    [ecf sendRemoteControlECFGpioToggleRequest:request to:self.device.kaaEndpointId];
}


@end
