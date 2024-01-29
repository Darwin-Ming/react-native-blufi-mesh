//
//  RNBlufiMesh.h
//  EueApp
//
//  Created by liyi on 2023/5/9.
//  Copyright © 2023 Facebook. All rights reserved.
//

#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#import "RCTEventEmitter.h"
#else
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>
#endif

#import "EtouchPeripheral.h"
#import "EtouchBLEHelper.h"

@interface BlufiMesh : RCTEventEmitter <RCTBridgeModule>
@property(nonatomic, strong) EtouchBLEHelper *etouchBLEHelper;
@property(nonatomic, copy) NSMutableArray<EtouchPeripheral *> *peripheralArray;



@end
