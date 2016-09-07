//
//  MessageCell.h
//  EventDemo
//
//  Created by Anton Vilimets on 9/6/16.
//  Copyright © 2016 CYBERVISION INC. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface MessageCell : UITableViewCell

@property (weak, nonatomic) IBOutlet UILabel *msgLabel;

+ (NSString *)cellIdentitier;

@end
