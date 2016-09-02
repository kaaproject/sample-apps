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

#import "ViewController.h"
#import "KaaManager.h"

#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <Fabric/Fabric.h>
#import <TwitterKit/TwitterKit.h>

@import Kaa;

typedef NS_ENUM(int, AuthorizedNetwork) {
    AuthorizedNetworkFacebook,
    AuthorizedNetworkTwitter,
    AuthorizedNetworkGoogle
};

@interface ViewController () <FBSDKLoginButtonDelegate>

@property (weak, nonatomic) IBOutlet UIStackView *socialButtonsStackView;

@property (weak, nonatomic) IBOutlet FBSDKLoginButton *fbLoginButton;
@property (weak, nonatomic) IBOutlet TWTRLogInButton *twtrLogInButton;
@property (weak, nonatomic) IBOutlet FBSDKLoginButton *fb3loginbutton;
@property (weak, nonatomic) IBOutlet UIButton *twtrLogOutButton;

@property (nonatomic, strong) KaaManager *kaaManager;
@property (nonatomic) AuthorizedNetwork authorizedNetwork;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.twtrLogOutButton.hidden = YES;
    self.fbLoginButton.delegate = self;
        
    self.twtrLogInButton.loginMethods = TWTRLoginMethodAll;
    self.twtrLogInButton.logInCompletion = ^(TWTRSession *session, NSError *error) {
        if (session) {
            NSLog(@"signed in as %@", [session userName]);
            self.authorizedNetwork = AuthorizedNetworkTwitter;
            [self loggedInWithNetwork:AuthorizedNetworkTwitter];
        } else {
            NSLog(@"error: %@", [error localizedDescription]);
        }
    };
    
    self.kaaManager = [KaaManager sharedInstance];
}

- (void)loggedInWithNetwork:(AuthorizedNetwork)network {
    switch (network) {
        case AuthorizedNetworkFacebook:
            self.twtrLogInButton.hidden = YES;
            self.fb3loginbutton.hidden = YES;
            break;
            
        case AuthorizedNetworkTwitter:
            self.fbLoginButton.hidden = YES;
            self.twtrLogInButton.hidden = YES;
            self.fb3loginbutton.hidden = YES;
            self.twtrLogOutButton.hidden = NO;
            break;
            
        case AuthorizedNetworkGoogle:
            self.fbLoginButton.hidden = YES;
            self.twtrLogInButton.hidden = YES;
            break;
            
        default:
            break;
    }
}

#pragma mark - FBSDKLoginButtonDelegate

- (void)loginButton:(FBSDKLoginButton *)loginButton didCompleteWithResult:(FBSDKLoginManagerLoginResult *)result error:(NSError *)error {
    self.twtrLogInButton.hidden = YES;
    self.fb3loginbutton.hidden = YES;
}

- (void)loginButtonDidLogOut:(FBSDKLoginButton *)loginButton {
    self.twtrLogInButton.hidden = NO;
    self.fb3loginbutton.hidden = NO;
}

#pragma mark - Actions

- (IBAction)twtrLogOutButtonPressed:(id)sender {
    TWTRSessionStore *store = [[Twitter sharedInstance] sessionStore];
    NSString *userID = store.session.userID;
    
    [store logOutUserID:userID];
    
    self.fbLoginButton.hidden = NO;
    self.twtrLogInButton.hidden = NO;
    self.fb3loginbutton.hidden = NO;
    self.twtrLogOutButton.hidden = YES;
}

@end
