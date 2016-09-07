//
//  MessageCell.m
//  EventDemo
//
//  Created by Anton Vilimets on 9/6/16.
//  Copyright © 2016 CYBERVISION INC. All rights reserved.
//

#import "MessageCell.h"

@implementation MessageCell

- (void)awakeFromNib {
    [super awakeFromNib];
    // Initialization code
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

+ (NSString *)cellIdentitier
{
    return @"MessageCell";
}

@end
