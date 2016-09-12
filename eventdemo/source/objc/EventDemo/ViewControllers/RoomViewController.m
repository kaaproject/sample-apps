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

#import "RoomViewController.h"
#import "ChatClientManager.h"
#import "MessageCell.h"


@implementation RoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = self.roomName;
    self.tableView.rowHeight = UITableViewAutomaticDimension;
    self.tableView.estimatedRowHeight = 44;

    [self setupNotifications];
    [self updateMessages];
}

- (void)updateMessages {
    self.messages = [[ChatClientManager sharedManager] messagesForRoom:self.roomName];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.tableView reloadData];
    });
}

#pragma mark - UITableViewDatasource methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.messages.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    MessageCell *cell = (MessageCell *)[tableView dequeueReusableCellWithIdentifier:[MessageCell cellIdentitier] forIndexPath:indexPath];
    cell.msgLabel.text = self.messages[indexPath.row];
    return cell;
}

- (void)keyboardWillShow:(NSNotification *)notif {
    CGRect keyboardFrame = [notif.userInfo[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    self.bottomOffsetConstraint.constant = keyboardFrame.size.height;
    [self.view layoutIfNeeded];
}

- (void)keyboardWillHide {
    self.bottomOffsetConstraint.constant = 0;
    [self.view layoutIfNeeded];
}

#pragma mark - Actions

- (IBAction)sendPressed:(id)sender {
    [[ChatClientManager sharedManager] sendMessage:self.messageTextField.text
                                              room:self.roomName];
    self.messageTextField.text = @"";
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

- (void)setupNotifications {
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide) name:UIKeyboardWillHideNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updateMessages) name:MessagesListUpdated object:nil];
}

@end
