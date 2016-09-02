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
#import <Google/SignIn.h>

@import Kaa;

typedef NS_ENUM(int, AuthorizedNetwork) {
    AuthorizedNetworkFacebook,
    AuthorizedNetworkTwitter,
    AuthorizedNetworkGoogle
};

@interface ViewController () <FBSDKLoginButtonDelegate, GIDSignInUIDelegate, GIDSignInDelegate>

@property (weak, nonatomic) IBOutlet UIStackView *socialButtonsStackView;

@property (weak, nonatomic) IBOutlet FBSDKLoginButton *fbLoginButton;
@property (weak, nonatomic) IBOutlet TWTRLogInButton *twtrLogInButton;
@property (weak, nonatomic) IBOutlet GIDSignInButton *googleLogInButton; 
@property (weak, nonatomic) IBOutlet UIButton *logOutButton;

@property (nonatomic, strong) KaaManager *kaaManager;
@property (nonatomic) AuthorizedNetwork authorizedNetwork;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.logOutButton.hidden = YES;
    self.googleLogInButton.colorScheme = kGIDSignInButtonColorSchemeDark;
    
    self.fbLoginButton.delegate = self;
    [GIDSignIn sharedInstance].uiDelegate = self;
    [GIDSignIn sharedInstance].delegate = self;
    
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
            self.googleLogInButton.hidden = YES;
            break;
            
        case AuthorizedNetworkTwitter:
        case AuthorizedNetworkGoogle:
            self.fbLoginButton.hidden = YES;
            self.twtrLogInButton.hidden = YES;
            self.googleLogInButton.hidden = YES;
            self.logOutButton.hidden = NO;
            break;
            
        default:
            break;
    }
}

#pragma mark - FBSDKLoginButtonDelegate

- (void)loginButton:(FBSDKLoginButton *)loginButton didCompleteWithResult:(FBSDKLoginManagerLoginResult *)result error:(NSError *)error {
    self.twtrLogInButton.hidden = YES;
    self.googleLogInButton.hidden = YES;
}

- (void)loginButtonDidLogOut:(FBSDKLoginButton *)loginButton {
    self.twtrLogInButton.hidden = NO;
    self.googleLogInButton.hidden = NO;
}

#pragma mark - GIDSignInDelegate

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {
    [self loggedInWithNetwork:AuthorizedNetworkGoogle];
}

#pragma mark - Actions

- (IBAction)logOutBtnPressed:(id)sender {
    if (self.authorizedNetwork == AuthorizedNetworkTwitter) {
        TWTRSessionStore *store = [[Twitter sharedInstance] sessionStore];
        NSString *userID = store.session.userID;
        
        [store logOutUserID:userID];
    } else {
        [[GIDSignIn sharedInstance] signOut];
    }
    
    self.fbLoginButton.hidden = NO;
    self.twtrLogInButton.hidden = NO;
    self.googleLogInButton.hidden = NO;
    self.logOutButton.hidden = YES;
}

@end
