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

typedef NS_ENUM(int, AuthorizationLabel) {
    AuthorizationLabelSocial,
    AuthorizationLabelKaa
};

@import Kaa;

@interface ViewController () <FBSDKLoginButtonDelegate, GIDSignInUIDelegate, GIDSignInDelegate, UserAttachDelegate, OnDetachEndpointOperationDelegate, OnAttachEndpointOperationDelegate>



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
    self.attachView.hidden = YES;
    self.googleLogInButton.colorScheme = kGIDSignInButtonColorSchemeDark;
    
    self.fbLoginButton.delegate = self;
    [GIDSignIn sharedInstance].uiDelegate = self;
    [GIDSignIn sharedInstance].delegate = self;
    
    self.twtrLogInButton.loginMethods = TWTRLoginMethodAll;
    self.twtrLogInButton.logInCompletion = ^(TWTRSession *session, NSError *error) {
        if (session) {
            NSLog(@"Signed in as %@", [session userName]);
            self.user = [[User alloc] initWithUserId:session.userID
                                               token:session.authToken
                                   authorizedNetwork:AuthorizedNetworkTwitter];
            [self updateSocialNetworkAuthorizationWithStatus:YES];
        } else {
            NSLog(@"error: %@", [error localizedDescription]);
        }
    };
    
    self.kaaManager = [KaaManager sharedInstance];
    [self.kaaManager startKaaClient];
}

- (void)updateSocialNetworkAuthorizationWithStatus:(BOOL)status {
    [self updateAuthorizationStatusForLabel:AuthorizationLabelSocial status:status];
    
    switch (self.user.network) {
        case AuthorizedNetworkFacebook:
            self.twtrLogInButton.hidden = status;
            self.googleLogInButton.hidden = status;
            break;
            
        case AuthorizedNetworkTwitter:
        case AuthorizedNetworkGoogle:
            self.fbLoginButton.hidden = status;
            self.twtrLogInButton.hidden = status;
            self.googleLogInButton.hidden = status;
            self.logOutButton.hidden = !status;
            break;
            
        default:
            break;
    }
    
    if (status) {
        [self.kaaManager attachUser:self.user delegate:self];
    } else {
        [self.kaaManager detachEndpoitWithDelegate:self];
        
        self.messagingView.hidden = YES;
        self.attachView.hidden = YES;
        self.chatTextView.text = @"";
    }
}

- (void)userHasAttached {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.messagingView.hidden = NO;
        self.attachView.hidden = NO;
    });
    //Obtain the event family factory.
    self.eventFamilyFactory = [self.kaaManager getEventFamilyFactory];
    //Obtain the concrete event family.
    self.vdecf = [self.kaaManager getEventClassFamily];
}

- (void)sendMessageWithText:(NSString *)text {
    KAAEventMessageEvent *message = [[KAAEventMessageEvent alloc] initWithMessage:[KAAUnion unionWithBranch:KAA_UNION_STRING_OR_NULL_BRANCH_0
                                                                                       data:text]];
    if (self.vdecf) {
        [self.vdecf sendKAAEventMessageEventToAll:message];
        [self updateMessagingUiWithText:text];
    }
}

- (void)updateMessagingUiWithText:(NSString *)text {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.messageTextField.text = @"";
        self.chatTextView.text = [NSString stringWithFormat:@"[%@] %@\n%@",
                                  [self.chatDateFormatter stringFromDate:[NSDate date]],
                                  text,
                                  self.chatTextView.text];
    });
}

- (void)updateAuthorizationStatusForLabel:(AuthorizationLabel)label status:(BOOL)status {
    dispatch_async(dispatch_get_main_queue(), ^{
        switch (label) {
            case AuthorizationLabelKaa: {
                self.kaaAuthorizationLabel.text = [NSString stringWithFormat:@"Kaa: %@", status ? @"YES" : @"NO"];
                break;
            }
            case AuthorizationLabelSocial: {
                self.socialNetworkAuthorizationLabel.text = [NSString stringWithFormat:@"Social network: %@", status ? @"YES" : @"NO"];
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
        self.user = [[User alloc] initWithUserId:result.token.userID
                                           token:result.token.tokenString
                               authorizedNetwork:AuthorizedNetworkFacebook];
        [self updateSocialNetworkAuthorizationWithStatus:YES];
    }
}

- (void)loginButtonDidLogOut:(FBSDKLoginButton *)loginButton {
    [self updateSocialNetworkAuthorizationWithStatus:NO];
}

#pragma mark - GIDSignInDelegate

- (void)signIn:(GIDSignIn *)signIn didSignInForUser:(GIDGoogleUser *)user withError:(NSError *)error {
    if (user) {
        NSLog(@"Signed in as %@", user.profile.name);
        self.user = [[User alloc] initWithUserId:user.userID
                                           token:user.authentication.accessToken
                               authorizedNetwork:AuthorizedNetworkGoogle];
        [self updateSocialNetworkAuthorizationWithStatus:YES];
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

#pragma mark - OnAttachEndpointOperationDelegate

- (void)onAttachResult:(SyncResponseResultType)result withEndpointKeyHash:(EndpointKeyHash *)endpointKeyHash {
    dispatch_async(dispatch_get_main_queue(), ^{
        switch (result) {
            case SYNC_RESPONSE_RESULT_TYPE_SUCCESS:
                self.chatTextView.text = [NSString stringWithFormat:@"Endpoint with key hash %@ joined chat\n%@", endpointKeyHash.keyHash, self.chatTextView.text];
                break;
                
            case SYNC_RESPONSE_RESULT_TYPE_FAILURE:
                self.chatTextView.text = [NSString stringWithFormat:@"Failed to add endpoint to chat\n%@", self.chatTextView.text];
                break;
                
            default:
                break;
        }
    });
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
    
    [self updateSocialNetworkAuthorizationWithStatus:NO];
}

- (IBAction)sendButtonTapped:(id)sender {
    if (self.messageTextField.text.length > 0) {
        [self sendMessageWithText:self.messageTextField.text];
        [self.messageTextField resignFirstResponder];
    }
}

- (IBAction)attachButtonPressed:(id)sender {
    if (self.endpointAccessTokenTextField.text.length > 0) {
        [self.kaaManager assistedAttachWithAccessToken:self.endpointAccessTokenTextField.text delegate:self];
        self.endpointAccessTokenTextField.text = @"";
    }
}

@end
