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

#import <CoreTelephony/CTCarrier.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>

#import "AppDelegate.h"
#import "ViewController.h"
#import "DefaultKaaPlatformContext.h"
#import "DefaultProfileManager.h"
#import "DefaultProfileTransport.h"
#import "DefaultLogUploadStrategy.h"
#import "RecordCountLogUploadStrategy.h"

@import CoreTelephony;

static CLLocationManager *locationManager;

#define UNDEFINED -1

@interface ViewController () <CLLocationManagerDelegate,LogDeliveryDelegate>

@property (nonatomic, weak) IBOutlet UILabel *operatorLabel;
@property (nonatomic, weak) IBOutlet UILabel *operatorNameLabel;
@property (nonatomic, weak) IBOutlet UILabel *mobileCountryCode;
@property (nonatomic, weak) IBOutlet UILabel *isoCountryCode;
@property (nonatomic, weak) IBOutlet UILabel *signalStreigthLabel;
@property (nonatomic, weak) IBOutlet UILabel *longtitudeLabel;
@property (nonatomic, weak) IBOutlet UILabel *latitudeLabel;
@property (nonatomic, weak) IBOutlet UILabel *logTimeLabel;
@property (nonatomic, weak) IBOutlet UILabel *logCountLabel;
@property (nonatomic, weak) IBOutlet UILabel *deliveredLogsLabel;

@property (nonatomic, weak) AppDelegate *appDelegate;
@property (nonatomic, strong) CTTelephonyNetworkInfo *telephoneNetworkManager;
@property (nonatomic, weak) id<KaaClient> kaaClient;
@property (nonatomic) NSInteger sentLogCount;
@property (nonatomic) int64_t lastLogTime;
@property (nonatomic) NSInteger deliveredLogsCount;

@end

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self startUpdatingLocation];
    self.telephoneNetworkManager = [[CTTelephonyNetworkInfo alloc] init];
    
    /*
     * Initialize the Kaa client using the Platform context.
     */
    self.appDelegate = [[UIApplication sharedApplication] delegate];
    self.kaaClient = self.appDelegate.kaaClient;
    [self.kaaClient setLogUploadStrategy:[[RecordCountLogUploadStrategy alloc] initWithCountThreshold:1]];
    [self.kaaClient setLogDeliveryDelegate:self];
    [self.kaaClient start];
}

#pragma mark - Log delivery delegate methods

- (void)onLogDeliverySuccessWithBucketInfo:(BucketInfo *)bucketInfo {
    NSLog(@"Bucket with id [%d] has been successfully delivered", bucketInfo.bucketId);
    self.deliveredLogsCount += bucketInfo.logCount;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.deliveredLogsLabel.text = [NSString stringWithFormat:@"%ld", (long)self.deliveredLogsCount];
    });
}

- (void)onLogDeliveryFailureWithBucketInfo:(BucketInfo *)bucketInfo {
    NSLog(@"Bucket [%d] delivery failed", bucketInfo.bucketId);
}

-(void)onLogDeliveryTimeoutWithBucketInfo:(BucketInfo *)bucketInfo {
    NSLog(@"Timeout on log delivery. Bucket [%d]", bucketInfo.bucketId);
}

#pragma mark - Apps methods

- (void)sendLog {
    if (self.appDelegate.isClientStarted) {
        self.sentLogCount++;
        double timestamp = [[NSDate date] timeIntervalSince1970];
        self.lastLogTime = (int64_t)(timestamp*1000);
        
        /*
         * Create an instance of a cell monitor log record and populate it with the latest values.
         */
        KAACellMonitorLog *cellMonitorLog = [[KAACellMonitorLog alloc] init];
        cellMonitorLog.logTime = self.lastLogTime;
        CTCarrier *carrier = [self.telephoneNetworkManager subscriberCellularProvider];
        NSString *operatorCode = [carrier mobileNetworkCode];
        if (operatorCode) {
            [cellMonitorLog setNetworkOperatorCode:[operatorCode intValue]];
        } else {
            [cellMonitorLog setNetworkOperatorCode:(int32_t)UNDEFINED];
        }
        [cellMonitorLog setNetworkOperatorName:[self carrierName]];
        self.operatorLabel.text = operatorCode;
        self.operatorNameLabel.text = [self carrierName];
        self.mobileCountryCode.text = [carrier mobileCountryCode];
        self.isoCountryCode.text = [carrier isoCountryCode];
        
        int cid = UNDEFINED;
        int lac = UNDEFINED;
        int signalStreigth =  UNDEFINED;
        
        signalStreigth = [self signalStreigth];
        self.signalStreigthLabel.text = [NSString stringWithFormat:@"%d", signalStreigth];
        
        [cellMonitorLog setGsmCellId:cid];
        [cellMonitorLog setGsmLac:lac];
        [cellMonitorLog setSignalStrength:signalStreigth];
        
        KAALocation *currentPhoneLocation = [[KAALocation alloc] init];
        currentPhoneLocation.longitude = locationManager.location.coordinate.longitude;
        currentPhoneLocation.latitude = locationManager.location.coordinate.latitude;
        [cellMonitorLog setPhoneGpsLocation:currentPhoneLocation];

        /*
         * Pass a cell monitor log record to the Kaa client. The Kaa client will upload
         * the log record according to the defined log upload strategy.
         */
        [self.kaaClient addLogRecord:cellMonitorLog];
        
    }
}

#pragma mark - LocationManager & delegate

- (void) startUpdatingLocation {
    CLLocationManager *manager = [ViewController getLocationManager];
    manager.delegate = self;
    manager.desiredAccuracy = kCLLocationAccuracyBest;
    
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0)
        [manager requestWhenInUseAuthorization];
    
    [manager startUpdatingLocation];
}

-(void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error{
    NSLog(@"Error: %@",error.description);
    self.longtitudeLabel.text = @"Unavaliable";
    self.latitudeLabel.text = @"Unavaliable";
}

-(void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations
{
    NSLog(@"Location changed");
    [self sendLog];
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateStyle:NSDateFormatterShortStyle];
    [dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
    self.logTimeLabel.text = [dateFormatter stringFromDate:[NSDate date]];
    self.logCountLabel.text = [NSString stringWithFormat:@"%ld", (long)self.sentLogCount];
    self.latitudeLabel.text = [NSString stringWithFormat:@"%10.8f", manager.location.coordinate.latitude];
    self.longtitudeLabel.text = [NSString stringWithFormat:@"%10.8f", manager.location.coordinate.longitude];
}

#pragma mark - Getting carrier name and signal streigth

- (int)signalStreigth {
    UIApplication *app = [UIApplication sharedApplication];
    NSArray *subviews = [[[app valueForKey:@"statusBar"] valueForKey:@"foregroundView"] subviews];
    NSString *dataNetworkItemView = nil;
    
    for (id subview in subviews) {
        if([subview isKindOfClass:[NSClassFromString(@"UIStatusBarSignalStrengthItemView") class]]) {
            dataNetworkItemView = subview;
            break;
        }
    }
    
    int signalStrength = [[dataNetworkItemView valueForKey:@"signalStrengthRaw"] intValue];
    
    return -signalStrength;
}

- (NSString *)carrierName {
    UIView* statusBar = [self statusBar];
    UIView* statusBarForegroundView = nil;
    for (UIView* view in statusBar.subviews) {
        if ([view isKindOfClass:NSClassFromString(@"UIStatusBarForegroundView")]) {
            statusBarForegroundView = view;
            break;
        }
    }
    UIView* statusBarServiceItem = nil;
    for (UIView* view in statusBarForegroundView.subviews) {
        if ([view isKindOfClass:NSClassFromString(@"UIStatusBarServiceItemView")]) {
            statusBarServiceItem = view;
            break;
        }
    }
    if (statusBarServiceItem) {
        id value = [statusBarServiceItem valueForKey:@"_serviceString"];
        
        if ([value isKindOfClass:[NSString class]]) {
            return (NSString *)value;
        }
    }
    return @"Unavailable";
}

- (UIView *)statusBar {
    NSString *statusBarString = [NSString stringWithFormat:@"%@ar", @"_statusB"];
    return [[UIApplication sharedApplication] valueForKey:statusBarString];
}

+ (CLLocationManager *)getLocationManager {
    static dispatch_once_t locationManagerToken;
    dispatch_once(&locationManagerToken, ^{
        locationManager = [[CLLocationManager alloc] init];
    });
    return locationManager;
}

@end
