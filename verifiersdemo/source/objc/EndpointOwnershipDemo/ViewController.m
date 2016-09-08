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
#import "User.h"

#import <FBSDKLoginKit/FBSDKLoginKit.h>
#import <FBSDKCoreKit/FBSDKAccessToken.h>
#import <Fabric/Fabric.h>
#import <TwitterKit/TwitterKit.h>
#import <Google/SignIn.h>

typedef NS_ENUM(int, AuthorizationLabel) {
    AuthorizationLabelSocial,
    AuthorizationLabelKaa
};

@import Kaa;

@interface ViewController () <FBSDKLoginButtonDelegate, GIDSignInUIDelegate, GIDSignInDelegate, UserAttachDelegate, OnDetachEndpointOperationDelegate>

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

@property (nonatomic, strong) KaaManager *kaaManager;
@property (nonatomic, strong) User *user;
@property (nonatomic, strong) EventFamilyFactory *eventFamilyFactory;
@property (nonatomic, strong) VerifiersDemoEventClassFamily *vdecf;
@property (nonatomic, strong) NSDateFormatter *chatDateFormatter;
@property (nonatomic) AuthorizedNetwork authorizedNetwork;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.chatDateFormatter = [[NSDateFormatter alloc] init];
    [self.chatDateFormatter setDateFormat:@"HH:mm dd.MM.yy"];
    
    self.logOutButton.hidden = YES;
    self.messagingView.hidden = YES;
    self.googleLogInButton.colorScheme = kGIDSignInButtonColorSchemeDark;
    
    self.fbLoginButton.delegate = self;
    [GIDSignIn sharedInstance].uiDelegate = self;
    [GIDSignIn sharedInstance].delegate = self;
    
    self.twtrLogInButton.loginMethods = TWTRLoginMethodAll;
    self.twtrLogInButton.logInCompletion = ^(TWTRSession *session, NSError *error) {
        if (session) {
            NSLog(@"Signed in as %@", [session userName]);
            self.user = [[User alloc] initWithUserId:session.userID token:session.authToken authorizedNetwork:AuthorizedNetworkTwitter];
            [self userHasLoggedIn];
        } else {
            NSLog(@"error: %@", [error localizedDescription]);
        }
    };
    
    self.kaaManager = [KaaManager sharedInstance];
    [self.kaaManager startKaaClient];
}

- (void)userHasLoggedIn {
    [self.kaaManager attachUser:self.user delegate:self];
    [self updateAuthorizationStatusForLabel:AuthorizationLabelSocial status:YES];
    self.messagingView.hidden = NO;
    
    switch (self.user.network) {
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

- (void)userHasLoggedOut {
    [self.kaaManager detachEndpoitWithDelegate:self];
    
    [self updateAuthorizationStatusForLabel:AuthorizationLabelSocial status:NO];
    self.messagingView.hidden = YES;
    self.chatTextView.text = @"";
    
    switch (self.user.network) {
        case AuthorizedNetworkFacebook:
            self.twtrLogInButton.hidden = NO;
            self.googleLogInButton.hidden = NO;
            break;
            
        case AuthorizedNetworkTwitter:
        case AuthorizedNetworkGoogle:
            self.fbLoginButton.hidden = NO;
            self.twtrLogInButton.hidden = NO;
            self.googleLogInButton.hidden = NO;
            self.logOutButton.hidden = YES;
            break;
            
        default:
            break;
    }
}

- (void)userHasAttached {
    //Obtain the event family factory.
    self.eventFamilyFactory = [self.kaaManager getEventFamilyFactory];
    //Obtain the concrete event family.
    self.vdecf = [self.kaaManager getEventClassFamily];
}

- (void)sendMessageWithText:(NSString *)text {
    MessageEvent *message = [[MessageEvent alloc] initWithMessage:[KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0 data:text]];
    if (self.vdecf) {
        [self.vdecf sendMessageEventToAll:message];
        [self updateMessagingUiWithText:text];
    }
}

- (void)updateMessagingUiWithText:(NSString *)text {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.messageTextField.text = @"";
        self.chatTextView.text = [NSString stringWithFormat:@"[%@] %@\n%@", [self.chatDateFormatter stringFromDate:[NSDate date]],text, self.chatTextView.text];
    });
}

- (void)updateAuthorizationStatusForLabel:(AuthorizationLabel)label status:(BOOL)status {
    dispatch_async(dispatch_get_main_queue(), ^{
        switch (label) {
            case AuthorizationLabelKaa: {
                if (status) {
                    self.kaaAuthorizationLabel.text = [NSString stringWithFormat:@"Kaa: YES"];
                } else {
                    self.kaaAuthorizationLabel.text = [NSString stringWithFormat:@"Kaa: NO"];
                }
                break;
            }
            case AuthorizationLabelSocial: {
                if (status) {
                    self.socialNetworkAuthorizationLabel.text = [NSString stringWithFormat:@"Social network: YES"];
                } else {
                    self.socialNetworkAuthorizationLabel.text = [NSString stringWithFormat:@"Social network: NO"];
                }
                break;
            }
                
            default:
                break;
        }
    });
}

#pragma mark - FBSDKLoginButtonDelegate

- (void)loginButton:(FBSDKLoginButton *)loginButton didCompleteWithResult:(FBSDKLoginManagerLoginResult *)result error:(NSError *)error {
    if (result.token) {
        NSLog(@"Signed in as user with id %@", result.token.userID);
        self.user = [[User alloc] initWithUserId:result.token.userID token:result.token.tokenString authorizedNetwork:AuthorizedNetworkFacebook];
        [self userHasLoggedIn];
    }
}

- (void)loginButtonDidLogOut:(FBSDKLoginButton *)loginButton {
    [self userHasLoggedOut];
}

#pragma mark - GIDSignInDelegate

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {
    if (user) {
        NSLog(@"Signed in as %@", user.profile.name);
        self.user = [[User alloc] initWithUserId:user.userID token:user.authentication.accessToken authorizedNetwork:AuthorizedNetworkGoogle];
        [self userHasLoggedIn];
    }
}

#pragma mark - UserAttachDelegate

- (void)onAttachResult:(UserAttachResponse *)response {
    switch (response.result) {
        case SYNC_RESPONSE_RESULT_TYPE_SUCCESS:
            NSLog(@"User attach result: success.");
            [self userHasAttached];
            [self updateAuthorizationStatusForLabel:AuthorizationLabelKaa status:YES];
            break;
            
        case SYNC_RESPONSE_RESULT_TYPE_FAILURE:
            NSLog(@"User attach result type: failure.");
            break;
            
        default:
            break;
    }
    if (response.errorCode.branch == KAA_UNION_USER_ATTACH_ERROR_CODE_OR_NULL_BRANCH_0) {
        NSLog(@"Error code: %@, error reason: %@", response.errorCode.data, response.errorReason.data);
    }
}

#pragma mark - OnDetachEndpointOperationDelegate

- (void)onDetachResult:(SyncResponseResultType)result {
    switch (result) {
        case SYNC_RESPONSE_RESULT_TYPE_SUCCESS:
            NSLog(@"Endpoint detach result: success");
            [self updateAuthorizationStatusForLabel:AuthorizationLabelKaa status:NO];
            break;
            
        case SYNC_RESPONSE_RESULT_TYPE_FAILURE:
            NSLog(@"Endpoint detach result: failure");
            break;
            
        case SYNC_RESPONSE_RESULT_TYPE_PROFILE_RESYNC:
            NSLog(@"Endpoint detach result: profile resync");
            break;
            
        case SYNC_RESPONSE_RESULT_TYPE_REDIRECT:
            NSLog(@"Endpoint detach result: redirect");
            break;
            
        default:
            break;
    }
}

#pragma mark - Actions

- (IBAction)logOutBtnPressed:(id)sender {
    if (self.user.network == AuthorizedNetworkTwitter) {
        TWTRSessionStore *store = [[Twitter sharedInstance] sessionStore];
        NSString *userID = store.session.userID;
        
        [store logOutUserID:userID];
    } else {
        [[GIDSignIn sharedInstance] signOut];
    }
    
    [self userHasLoggedOut];
}

- (IBAction)sendButtonTapped:(id)sender {
    [self sendMessageWithText:self.messageTextField.text];
}


@end
