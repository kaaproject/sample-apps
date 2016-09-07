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

#import "RoomsListViewController.h"
#import "ChatClientManager.h"
#import "RoomCell.h"
#import "RoomViewController.h"

@interface RoomsListViewController ()
{
    NSArray *roomsList;
}


@end

@implementation RoomsListViewController

- (void)viewDidLoad
{
    [super viewDidLoad];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateRoomsList) name:RoomsListUpdated object:nil];
}


- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self updateRoomsList];
}


- (void)updateRoomsList
{
    roomsList = [[ChatClientManager sharedManager] roomsList];
    [self.tableView reloadData];
}


- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return roomsList.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    RoomCell *cell = (RoomCell *)[tableView dequeueReusableCellWithIdentifier:[RoomCell cellIdentitier] forIndexPath:indexPath];
    NSString *room = roomsList[indexPath.row];
    BOOL isJoined = [[ChatClientManager sharedManager] isJoinedRoom:room];
    [cell setName:room isJoined:isJoined];
    return cell;
}


- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    RoomViewController *roomVC = [self.storyboard instantiateViewControllerWithIdentifier:@"RoomViewController"];
    roomVC.roomName = roomsList[indexPath.row];
    [self.navigationController pushViewController:roomVC animated:YES];
}


- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return UITableViewCellEditingStyleDelete;
}


- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath
{
    [[ChatClientManager sharedManager] deleteRoom:roomsList[indexPath.row]];
}


- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath
{
    return [[ChatClientManager sharedManager] canRemoveRoom:roomsList[indexPath.row]];
}


- (IBAction)editPressed:(UIBarButtonItem *)sender
{
    [self.tableView setEditing:!self.tableView.isEditing animated:YES];
}


- (IBAction)addPressed:(id)sender
{
    UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"Enter room name:"
                                                                   message:nil preferredStyle:UIAlertControllerStyleAlert];
    [alert addTextFieldWithConfigurationHandler:nil];
    
    UIAlertAction *okAction = [UIAlertAction actionWithTitle:@"Ok" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
        NSString *name = alert.textFields[0].text;
        [[ChatClientManager sharedManager] addRoom:name];
    }];
    
    [alert addAction:okAction];
    
    [alert addAction:[UIAlertAction actionWithTitle:@"Cancel"
                                              style:UIAlertActionStyleCancel
                                            handler:nil]];
    [self presentViewController:alert animated:YES completion:nil];
}

@end
