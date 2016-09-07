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

@interface RoomViewController () <UITextFieldDelegate>
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *bottomOffsetConstraint;

@end

@implementation RoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.title = _roomName;
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide) name:UIKeyboardWillHideNotification object:nil];
    BOOL isJoined = [[ChatClientManager sharedManager] isJoinedRoom:_roomName];
    _leaveButton.hidden = isJoined;
}



- (IBAction)sendPressed:(id)sender
{
    [_messageTextField resignFirstResponder];
    _messageTextField.text = @"";
    [[ChatClientManager sharedManager] sendMessage:_messageTextField.text
                                               room:_roomName];
}

- (IBAction)leavePressed:(id)sender
{
    [[ChatClientManager sharedManager] leaveRoom:_roomName];
    [self.navigationController popViewControllerAnimated:YES];
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [[ChatClientManager sharedManager] messagesForRoom:_roomName].count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    MessageCell *cell = (MessageCell *)[tableView dequeueReusableCellWithIdentifier:[MessageCell cellIdentitier] forIndexPath:indexPath];
    NSString *msg = [[ChatClientManager sharedManager] messagesForRoom:_roomName][indexPath.row];
    cell.msgLabel.text = msg;
    return cell;
}

- (void)keyboardWillShow:(NSNotification *)notif
{
    CGRect keyboardFrame = [notif.userInfo[UIKeyboardFrameEndUserInfoKey] CGRectValue];
    _bottomOffsetConstraint.constant = keyboardFrame.size.height;
    [self.view layoutIfNeeded];
}

- (void)keyboardWillHide
{
    _bottomOffsetConstraint.constant = 0;
    [self.view layoutIfNeeded];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
    [textField resignFirstResponder];
    return YES;
}

@end
