//
//  BlufiScanResponse.h
//
//  Created by liyi on 2023/8/3.
//  Copyright © 2023 einter. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BlufiScanResponse : NSObject

@property(assign, nonatomic)int type;
@property(strong, nonatomic)NSString *ssid;
@property(assign, nonatomic)int8_t rssi;

@end

NS_ASSUME_NONNULL_END
