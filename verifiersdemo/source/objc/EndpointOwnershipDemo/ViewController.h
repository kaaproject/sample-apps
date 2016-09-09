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

#import <UIKit/UIKit.h>

#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <FBSDKCoreKit/FBSDKAccessToken.h>
#import <Fabric/Fabric.h>
#import <TwitterKit/TwitterKit.h>
#import <Google/SignIn.h>

@interface ViewController : UIViewController

@property (weak, nonatomic) IBOutlet UIStackView *socialButtonsStackView;

@property (weak, nonatomic) IBOutlet FBSDKLoginButton *fbLoginButton;
@property (weak, nonatomic) IBOutlet TWTRLogInButton *twtrLogInButton;
@property (weak, nonatomic) IBOutlet GIDSignInButton *googleLogInButton;
@property (weak, nonatomic) IBOutlet UIButton *logOutButton;

@property (weak, nonatomic) IBOutlet UILabel *socialNetworkAuthorizationLabel;
@property (weak, nonatomic) IBOutlet UILabel *kaaAuthorizationLabel;

@property (weak, nonatomic) IBOutlet UIView *messagingView;
@property (weak, nonatomic) IBOutlet UITextField *messageTextField;
@property (weak, nonatomic) IBOutlet UITextView *chatTextView;

@property (weak, nonatomic) IBOutlet UITextField *endpointAccessTokenTextField;
@property (weak, nonatomic) IBOutlet UIView *attachView;

@end
