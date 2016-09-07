//
//  RoomCell.h
//  EventDemo
//
//  Created by Anton Vilimets on 9/6/16.
//  Copyright © 2016 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface RoomCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UIButton *joinButton;
@property (weak, nonatomic) IBOutlet UILabel *nameLabel;

+ (NSString *)cellIdentitier;
- (void)setName:(NSString *)name isJoined:(BOOL)isJoined;

@end
