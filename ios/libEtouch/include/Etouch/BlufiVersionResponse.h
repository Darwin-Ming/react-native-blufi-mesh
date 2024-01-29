//
//  BlufiVersionResponse.h
//
//  Created by liyi on 2023/8/3.
//  Copyright © 2020 elinter. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BlufiVersionResponse : NSObject

@property(assign, nonatomic)Byte bigVer;
@property(assign, nonatomic)Byte smallVer;

- (NSString *)getVersionString;

@end

NS_ASSUME_NONNULL_END
