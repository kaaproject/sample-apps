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

/*
 * This header provides several bindings for x86_64 target that abstracts
 * an implementation of its features. Right now it contains only
 * console and target initialisation routines, but it must be extended
 * if required.
 *
 * Note that by x86 target is understood a system with support of
 * stdc library and most likely running full-featured OS.
 *
 * TODO: rename it to something like 'linux' or 'host' ?
 */
#ifndef X86_64_SUPPORT_H
#define X86_64_SUPPORT_H

#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

/* Demo print routine. Default printf exists for x86_64 target. */
#define demo_printf(msg, ...) printf((msg), ##__VA_ARGS__)

/* Initialises a target. Zero value means success, negative - errors. */
static inline int target_initialise(void)
{
    /* This target do not require special initialisation. */
    return 0;
}

#endif // X86_64_SUPPORT_H

