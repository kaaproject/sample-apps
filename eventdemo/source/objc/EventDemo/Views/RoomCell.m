//
//  RoomCell.m
//  EventDemo
//
//  Created by Anton Vilimets on 9/6/16.
//  Copyright © 2016 CYBERVISION INC. All rights reserved.
//

#import "RoomCell.h"
#import "ChatClientManager.h"

@implementation RoomCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

+ (NSString *)cellIdentitier
{
    return @"RoomCell";
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

}

- (void)setName:(NSString *)name isJoined:(BOOL)isJoined
{
    _nameLabel.text = name;
    _joinButton.hidden = isJoined;
}

- (IBAction)joinPressed:(id)sender
{
    [[ChatClientManager sharedManager] joinRoom:_nameLabel.text];
}

@end
