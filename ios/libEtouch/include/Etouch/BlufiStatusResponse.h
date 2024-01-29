//
//  BlufiStatusResponse.h
//  Created by liyi on 2023/8/3.
//  Copyright © 2023 einter. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BlufiConstants.h"

NS_ASSUME_NONNULL_BEGIN

@interface BlufiStatusResponse : NSObject

@property(assign, nonatomic)OpMode opMode;

@property(assign, nonatomic)SoftAPSecurity softApSecurity;
@property(assign, nonatomic)int softApConnectionCount;
@property(assign, nonatomic)int softApMaxConnection;
@property(assign, nonatomic)int softApChannel;
@property(strong, nonatomic)NSString *softApPassword;
@property(strong, nonatomic)NSString *softApSsid;

@property(assign, nonatomic)int staConnectionStatus;
@property(strong, nonatomic)NSString *staBssid;
@property(strong, nonatomic)NSString *staSsid;
@property(strong, nonatomic)NSString *staPassword;

- (BOOL)isStaConnectWiFi;

- (NSString *)getStatusInfo;

@end

NS_ASSUME_NONNULL_END
