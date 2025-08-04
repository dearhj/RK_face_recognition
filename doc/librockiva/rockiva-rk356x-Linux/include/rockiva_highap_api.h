/****************************************************************************
 *
 *    Copyright (c) 2022 by Rockchip Corp.  All rights reserved.
 *
 *    The material in this file is confidential and contains trade secrets
 *    of Rockchip Corporation. This is proprietary information owned by
 *    Rockchip Corporation. No part of this work may be disclosed,
 *    reproduced, copied, transmitted, or used in any way for any purpose,
 *    without the express written permission of Rockchip Corporation.
 *
 *****************************************************************************/

#ifndef __ROCKIVA_HIGHAP_API_H__
#define __ROCKIVA_HIGHAP_API_H__

#include "rockiva_common.h"

#ifdef __cplusplus
extern "C" {
#endif
/* ------------------------------------------------------------------ */

#define ROCKIVA_HIGHAP_MAX_NUM (128)      /* 高空拋物轨迹最大数目 */

/* ---------------------------规则配置----------------------------------- */

/* 高空拋物检测业务初始化参数配置 */
typedef struct
{
    uint8_t mode;               /* 输入图像大小（0: 2560*1440; 1: 1920*1080） */
    RockIvaAreas detectAreas;   /* 检测区域 */
} RockIvaHighapTaskParam;


/* -------------------------- 算法处理结果 --------------------------- */

/* 高空拋物检测处理结果 */
typedef struct
{
    uint32_t trackNum;                                   /* 高空拋物检测轨迹个数 */
    RockIvaDetectResult track[ROCKIVA_HIGHAP_MAX_NUM]; /* 高空拋物检测轨迹结果 */
} RockIvaHighapResult;

/**
 * @brief 高空拋物检测结果回调函数
 *
 * result 结果
 * status 状态码
 * userdata 用户自定义数据
 */
typedef void (*RockIvaHighapCallback)(const RockIvaHighapResult* result, const RockIvaExecuteStatus status,
                                     void* userdata);

/**
 * @brief 初始化
 *
 * @param handle [INOUT] 初始化完成的handle
 * @param initParams [IN] 初始化参数
 * @param resultCallback [IN] 回调函数
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_HIGHAP_Init(RockIvaHandle handle, const RockIvaHighapTaskParam* initParams,
                                  const RockIvaHighapCallback callback);

/**
 * @brief 运行时重新配置(重新配置会导致内部的一些记录清空复位，但是模型不会重新初始化)
 * 
 * @param handle [IN] handle
 * @param initParams [IN] 配置参数
 * @return RockIvaRetCode 
 */
RockIvaRetCode ROCKIVA_HIGHAP_Reset(RockIvaHandle handle, const RockIvaHighapTaskParam* initParams);

/**
 * @brief 销毁
 *
 * @param handle [IN] handle
 * @return RockIvaRetCode
 */
RockIvaRetCode ROCKIVA_HIGHAP_Release(RockIvaHandle handle);

#ifdef __cplusplus
}
#endif /* end of __cplusplus */

#endif