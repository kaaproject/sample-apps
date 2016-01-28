/*
 * Copyright 2014-2015 CyberVision, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import "ConnectionAlert.h"

@implementation ConnectionAlert

+ (UIAlertController *)showAlertNoConnection {
    UIAlertController *alertController = [UIAlertController
                                          alertControllerWithTitle:@"Connection status"
                                          message:@"No connection"
                                          preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction* cancel = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction * action) {
                                                       [alertController dismissViewControllerAnimated:YES completion:nil];
                                                   }];
    [alertController addAction:cancel];
    return alertController;
}

+ (UIAlertController *)showAlertEmtpyEndpointId {
    UIAlertController *alertController = [UIAlertController
                                          alertControllerWithTitle:@"Empty endpoint id"
                                          message:@"Endpoint id can't be empty"
                                          preferredStyle:UIAlertControllerStyleAlert];
    UIAlertAction* cancel = [UIAlertAction actionWithTitle:@"OK" style:UIAlertActionStyleDefault
                                                   handler:^(UIAlertAction * action) {
                                                       [alertController dismissViewControllerAnimated:YES completion:nil];
                                                   }];
    [alertController addAction:cancel];
    return alertController;

}

@end
