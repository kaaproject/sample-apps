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

#include <time.h>
#include "cc32xx_support.h"

#define DEMO_UNUSED(x) if(x){}

#define HTTP_BUF_LEN 1440
#define FIRMWARE_FILE_PATH "/sys/mcuimg1.bin"

unsigned long  g_ulStatus = 0;//SimpleLink Status
unsigned long  g_ulGatewayIP = 0; //Network Gateway IP address
unsigned char  g_ucConnectionStatus = 0;
unsigned char  g_ucSimplelinkstarted = 0;
unsigned long  g_ulIpAddr = 0;

static unsigned crc_32_tab[] = {
    0x00000000, 0x77073096, 0xee0e612c, 0x990951ba, 0x076dc419, 0x706af48f,
    0xe963a535, 0x9e6495a3, 0x0edb8832, 0x79dcb8a4, 0xe0d5e91e, 0x97d2d988,
    0x09b64c2b, 0x7eb17cbd, 0xe7b82d07, 0x90bf1d91, 0x1db71064, 0x6ab020f2,
    0xf3b97148, 0x84be41de, 0x1adad47d, 0x6ddde4eb, 0xf4d4b551, 0x83d385c7,
    0x136c9856, 0x646ba8c0, 0xfd62f97a, 0x8a65c9ec, 0x14015c4f, 0x63066cd9,
    0xfa0f3d63, 0x8d080df5, 0x3b6e20c8, 0x4c69105e, 0xd56041e4, 0xa2677172,
    0x3c03e4d1, 0x4b04d447, 0xd20d85fd, 0xa50ab56b, 0x35b5a8fa, 0x42b2986c,
    0xdbbbc9d6, 0xacbcf940, 0x32d86ce3, 0x45df5c75, 0xdcd60dcf, 0xabd13d59,
    0x26d930ac, 0x51de003a, 0xc8d75180, 0xbfd06116, 0x21b4f4b5, 0x56b3c423,
    0xcfba9599, 0xb8bda50f, 0x2802b89e, 0x5f058808, 0xc60cd9b2, 0xb10be924,
    0x2f6f7c87, 0x58684c11, 0xc1611dab, 0xb6662d3d, 0x76dc4190, 0x01db7106,
    0x98d220bc, 0xefd5102a, 0x71b18589, 0x06b6b51f, 0x9fbfe4a5, 0xe8b8d433,
    0x7807c9a2, 0x0f00f934, 0x9609a88e, 0xe10e9818, 0x7f6a0dbb, 0x086d3d2d,
    0x91646c97, 0xe6635c01, 0x6b6b51f4, 0x1c6c6162, 0x856530d8, 0xf262004e,
    0x6c0695ed, 0x1b01a57b, 0x8208f4c1, 0xf50fc457, 0x65b0d9c6, 0x12b7e950,
    0x8bbeb8ea, 0xfcb9887c, 0x62dd1ddf, 0x15da2d49, 0x8cd37cf3, 0xfbd44c65,
    0x4db26158, 0x3ab551ce, 0xa3bc0074, 0xd4bb30e2, 0x4adfa541, 0x3dd895d7,
    0xa4d1c46d, 0xd3d6f4fb, 0x4369e96a, 0x346ed9fc, 0xad678846, 0xda60b8d0,
    0x44042d73, 0x33031de5, 0xaa0a4c5f, 0xdd0d7cc9, 0x5005713c, 0x270241aa,
    0xbe0b1010, 0xc90c2086, 0x5768b525, 0x206f85b3, 0xb966d409, 0xce61e49f,
    0x5edef90e, 0x29d9c998, 0xb0d09822, 0xc7d7a8b4, 0x59b33d17, 0x2eb40d81,
    0xb7bd5c3b, 0xc0ba6cad, 0xedb88320, 0x9abfb3b6, 0x03b6e20c, 0x74b1d29a,
    0xead54739, 0x9dd277af, 0x04db2615, 0x73dc1683, 0xe3630b12, 0x94643b84,
    0x0d6d6a3e, 0x7a6a5aa8, 0xe40ecf0b, 0x9309ff9d, 0x0a00ae27, 0x7d079eb1,
    0xf00f9344, 0x8708a3d2, 0x1e01f268, 0x6906c2fe, 0xf762575d, 0x806567cb,
    0x196c3671, 0x6e6b06e7, 0xfed41b76, 0x89d32be0, 0x10da7a5a, 0x67dd4acc,
    0xf9b9df6f, 0x8ebeeff9, 0x17b7be43, 0x60b08ed5, 0xd6d6a3e8, 0xa1d1937e,
    0x38d8c2c4, 0x4fdff252, 0xd1bb67f1, 0xa6bc5767, 0x3fb506dd, 0x48b2364b,
    0xd80d2bda, 0xaf0a1b4c, 0x36034af6, 0x41047a60, 0xdf60efc3, 0xa867df55,
    0x316e8eef, 0x4669be79, 0xcb61b38c, 0xbc66831a, 0x256fd2a0, 0x5268e236,
    0xcc0c7795, 0xbb0b4703, 0x220216b9, 0x5505262f, 0xc5ba3bbe, 0xb2bd0b28,
    0x2bb45a92, 0x5cb36a04, 0xc2d7ffa7, 0xb5d0cf31, 0x2cd99e8b, 0x5bdeae1d,
    0x9b64c2b0, 0xec63f226, 0x756aa39c, 0x026d930a, 0x9c0906a9, 0xeb0e363f,
    0x72076785, 0x05005713, 0x95bf4a82, 0xe2b87a14, 0x7bb12bae, 0x0cb61b38,
    0x92d28e9b, 0xe5d5be0d, 0x7cdcefb7, 0x0bdbdf21, 0x86d3d2d4, 0xf1d4e242,
    0x68ddb3f8, 0x1fda836e, 0x81be16cd, 0xf6b9265b, 0x6fb077e1, 0x18b74777,
    0x88085ae6, 0xff0f6a70, 0x66063bca, 0x11010b5c, 0x8f659eff, 0xf862ae69,
    0x616bffd3, 0x166ccf45, 0xa00ae278, 0xd70dd2ee, 0x4e048354, 0x3903b3c2,
    0xa7672661, 0xd06016f7, 0x4969474d, 0x3e6e77db, 0xaed16a4a, 0xd9d65adc,
    0x40df0b66, 0x37d83bf0, 0xa9bcae53, 0xdebb9ec5, 0x47b2cf7f, 0x30b5ffe9,
    0xbdbdf21c, 0xcabac28a, 0x53b39330, 0x24b4a3a6, 0xbad03605, 0xcdd70693,
    0x54de5729, 0x23d967bf, 0xb3667a2e, 0xc4614ab8, 0x5d681b02, 0x2a6f2b94,
    0xb40bbe37, 0xc30c8ea1, 0x5a05df1b, 0x2d02ef8d
};

typedef struct {
    char filename[255];
    unsigned int file_size;
    unsigned int hash;
} firmware_info_t;

static firmware_version_t cur_version = { MAJOR_VERSION, MINOR_VERSION, 0, CLASSIFIER_VERSION };

extern void (* const g_pfnVectors[])(void);

unsigned crc32(unsigned crc, const void *buf, size_t size)
{
    const unsigned char *p;

    p = buf;
    crc = crc ^ ~0U;//change

    while (size--)
        crc = crc_32_tab[(crc ^ *p++) & 0xFF] ^ (crc >> 8);

    return crc ^ ~0U;//change
}

void SimpleLinkWlanEventHandler(SlWlanEvent_t *pWlanEvent)
{
    UART_PRINT("SimpleLinkWlanEventHandler\r\n");
    switch(pWlanEvent->Event)
    {
        case SL_WLAN_CONNECT_EVENT:
        {
            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);

            //
            // Information about the connected AP (like name, MAC etc) will be
            // available in 'slWlanConnectAsyncResponse_t'-Applications
            // can use it if required
            //
            //  slWlanConnectAsyncResponse_t *pEventData = NULL;
            // pEventData = &pWlanEvent->EventData.STAandP2PModeWlanConnected;
            //

            UART_PRINT("[WLAN EVENT] STA Connected to the AP: %s\n\r",
                       pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
        }
        break;

        case SL_WLAN_DISCONNECT_EVENT:
        {
            slWlanConnectAsyncResponse_t*  pEventData = NULL;

            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_CONNECTION);
            CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            pEventData = &pWlanEvent->EventData.STAandP2PModeDisconnected;

            // If the user has initiated 'Disconnect' request,
            //'reason_code' is SL_USER_INITIATED_DISCONNECTION
            if(SL_USER_INITIATED_DISCONNECTION == pEventData->reason_code)
            {
                UART_PRINT("[WLAN EVENT]Device disconnected from the AP: %s\r\n",
                           pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
            }
            else
            {
                UART_PRINT("[WLAN ERROR]Device disconnected from the AP AP: %s\r\n",
                           pWlanEvent->EventData.STAandP2PModeWlanConnected.ssid_name);
            }
        }
        break;

        default:
        {
            UART_PRINT("[WLAN EVENT] Unexpected event [0x%x]\n\r",
                       pWlanEvent->Event);
        }
        break;
    }
}

void SimpleLinkNetAppEventHandler(SlNetAppEvent_t *pNetAppEvent)
{
    switch(pNetAppEvent->Event)
    {
        case SL_NETAPP_IPV4_IPACQUIRED_EVENT:
        {
            SlIpV4AcquiredAsync_t *pEventData = NULL;

            SET_STATUS_BIT(g_ulStatus, STATUS_BIT_IP_AQUIRED);

            //Ip Acquired Event Data
            pEventData = &pNetAppEvent->EventData.ipAcquiredV4;

            //Gateway IP address
            g_ulGatewayIP = pEventData->gateway;

            UART_PRINT("[NETAPP EVENT] IP Acquired: IP=%d.%d.%d.%d , "
            "Gateway=%d.%d.%d.%d\n\r",
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.ip,0),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,3),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,2),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,1),
            SL_IPV4_BYTE(pNetAppEvent->EventData.ipAcquiredV4.gateway,0));
        }
        break;

        default:
        {
            UART_PRINT("[NETAPP EVENT] Unexpected event [0x%x] \n\r",
                       pNetAppEvent->Event);
        }
        break;
    }
}

void SimpleLinkHttpServerCallback(SlHttpServerEvent_t *pHttpEvent,
                                  SlHttpServerResponse_t *pHttpResponse)
{
    DEMO_UNUSED(pHttpEvent)
    DEMO_UNUSED(pHttpResponse)
    // Unused in this application
}

void SimpleLinkGeneralEventHandler(SlDeviceEvent_t *pDevEvent)
{
    DEMO_UNUSED(pDevEvent)
    // Unused in this application
}

void SimpleLinkSockEventHandler(SlSockEvent_t *pSock)
{
    DEMO_UNUSED(pSock)
    // Unused in this application
}

void SimpleLinkPingReport(SlPingReport_t *report)
{
    SET_STATUS_BIT(g_ulStatus, STATUS_BIT_PING_DONE);
    UART_PRINT("ping %d\r\n", report->AvgRoundTime);
}

void BoardInit()
{
    MAP_IntVTableBaseSet((unsigned long)&g_pfnVectors[0]);

    MAP_IntMasterEnable();
    MAP_IntEnable(FAULT_SYSTICK);    

    PRCMCC3200MCUInit();    

    UDMAInit();
	
    MAP_PRCMPeripheralClkEnable(PRCM_UARTA0, PRCM_RUN_MODE_CLK);
    MAP_PinTypeUART(PIN_55, PIN_MODE_3);
    MAP_PinTypeUART(PIN_57, PIN_MODE_3);
	
    InitTerm();
}


void wlan_configure()
{
    sl_Start(NULL, NULL, NULL);

    // reset all network policies
    unsigned char ucpolicyVal;
    int ret;
    ret = sl_WlanPolicySet(SL_POLICY_CONNECTION,
                    SL_CONNECTION_POLICY(0,0,0,0,0),
                    &ucpolicyVal,
                    1 /*PolicyValLen*/);
    if(ret < 0)
    {
        LOOP_FOREVER();
    }

    sl_WlanSetMode(ROLE_STA);
    sl_WlanPolicySet(SL_POLICY_CONNECTION, SL_CONNECTION_POLICY(1, 0, 0, 0, 1), NULL, 0);
    sl_WlanProfileDel(0xFF);
    sl_WlanDisconnect();

    _WlanRxFilterOperationCommandBuff_t  RxFilterIdMask;// = {0};
    memset(&RxFilterIdMask, 0, sizeof(RxFilterIdMask));

    unsigned char ucVal = 0;
    unsigned char ucConfigOpt = 0;
    // Enable DHCP client
    sl_NetCfgSet(SL_IPV4_STA_P2P_CL_DHCP_ENABLE,1,1,&ucVal);
    // Disable scan
    ucConfigOpt = SL_SCAN_POLICY(0);
    sl_WlanPolicySet(SL_POLICY_SCAN , ucConfigOpt, NULL, 0);
    // Set Tx power level for station mode
    // Number between 0-15, as dB offset from max power - 0 will set max power
    unsigned char ucPower = 0;
    sl_WlanSet(SL_WLAN_CFG_GENERAL_PARAM_ID, WLAN_GENERAL_PARAM_OPT_STA_TX_POWER, 1, (unsigned char *)&ucPower);
    // Set PM policy to normal
    sl_WlanPolicySet(SL_POLICY_PM , SL_NORMAL_POLICY, NULL, 0);
    // Unregister mDNS services
    sl_NetAppMDNSUnRegisterService(0, 0);
    // Remove  all 64 filters (8*8)
    memset(RxFilterIdMask.FilterIdMask, 0xFF, 8);
    sl_WlanRxFilterSet(SL_REMOVE_RX_FILTER, (_u8 *)&RxFilterIdMask, sizeof(_WlanRxFilterOperationCommandBuff_t));
    sl_Stop(SL_STOP_TIMEOUT);
}

void wlan_scan()
{
    unsigned char ucpolicyOpt;
    union
    {
        unsigned char ucPolicy[4];
        unsigned int uiPolicyLen;
    }policyVal;

    ucpolicyOpt = SL_CONNECTION_POLICY(0, 0, 0, 0,0);
    sl_WlanPolicySet(SL_POLICY_CONNECTION , ucpolicyOpt, NULL, 0);
    ucpolicyOpt = SL_SCAN_POLICY(1);
    policyVal.uiPolicyLen = 10;
    sl_WlanPolicySet(SL_POLICY_SCAN , ucpolicyOpt, (unsigned char*)(policyVal.ucPolicy), sizeof(policyVal));
    MAP_UtilsDelay(8000000);

    Sl_WlanNetworkEntry_t netEntries[10];
    _i16 resultsCount = sl_WlanGetNetworkList(0,10,&netEntries[0]);
    for(int i=0; i< resultsCount; i++)
        UART_PRINT("ssid: %s\trssi: %d\tsec-t: %u\r\n",netEntries[i].ssid, netEntries[i].rssi, netEntries[i].sec_type);
}

int wlan_connect(const char *ssid, const char *pass, unsigned char sec_type)
{
    SlSecParams_t secParams = {0};
    long lRetVal = 0;

    secParams.Key = (signed char*)pass;
    secParams.KeyLen = strlen(pass);
    secParams.Type = sec_type;

    lRetVal = sl_WlanConnect((signed char*)ssid, strlen(ssid), 0, &secParams, 0);

    ASSERT_ON_ERROR(lRetVal);

    while((!IS_CONNECTED(g_ulStatus)) || (!IS_IP_ACQUIRED(g_ulStatus)))
        _SlNonOsMainLoopTask();

    SlDateTime_t dateTime= {0};
    dateTime.sl_tm_day =   1;          // Day of month (DD format) range 1-13
    dateTime.sl_tm_mon =   1;           // Month (MM format) in the range of 1-12
    dateTime.sl_tm_year =  1970;        // Year (YYYY format)
    dateTime.sl_tm_hour =  0;          // Hours in the range of 0-23
    dateTime.sl_tm_min =   0;          // Minutes in the range of 0-59
    dateTime.sl_tm_sec =   1;          // Seconds in the range of  0-59

    lRetVal = sl_DevSet(SL_DEVICE_GENERAL_CONFIGURATION, SL_DEVICE_GENERAL_CONFIGURATION_DATE_TIME,
              sizeof(SlDateTime_t),(unsigned char *)(&dateTime));
    ASSERT_ON_ERROR(lRetVal);

    return 0;
}

void net_ping(const char *host)
{
    SlPingStartCommand_t pingParams = {0};
    SlPingReport_t pingReport = {0};
    unsigned long ulIpAddr = 0;
    CLR_STATUS_BIT(g_ulStatus, STATUS_BIT_PING_DONE);

    // Set the ping parameters
    pingParams.PingIntervalTime = 1000;
    pingParams.PingSize = 20;
    pingParams.PingRequestTimeout = 3000;
    pingParams.TotalNumberOfAttempts = 3;
    pingParams.Flags = 0;
    pingParams.Ip = g_ulGatewayIP;

    UART_PRINT("ping host: %s\r\n", host);

    sl_NetAppDnsGetHostByName((signed char*)host, strlen(host), &ulIpAddr, SL_AF_INET);
    UART_PRINT("host ip: 0x%08X\r\n", host);
    pingParams.Ip = ulIpAddr;
    sl_NetAppPingStart((SlPingStartCommand_t*)&pingParams, SL_AF_INET, (SlPingReport_t*)&pingReport, SimpleLinkPingReport);

    while(!IS_PING_DONE(g_ulStatus))
        _SlNonOsMainLoopTask();
}

int recv_eagain(short sockId, void *pBuf, int Len, int flags, int max_eagain)
{
    int len;

    do
    {
        len = sl_Recv(sockId, pBuf, Len, flags);
        if (len != SL_EAGAIN) /*Try Again */
            break;
    } while(--max_eagain);

    return len;
}

int create_request(char *request, const char *method, const char *host_name, unsigned short port, const char *req_uri)
{
    strcpy(request, method);
    strcat(request, " ");

    if(req_uri && strlen(req_uri)) strcat(request, req_uri);
    else strcat(request, "/");

    strcat(request, " HTTP/1.1\r\nHost: ");
    strcat(request, host_name);
    if(port != 80)
    {
        char tmp[6];
        sprintf(tmp, "%d", port);

        strcat(request, ":");
        strcat(request, tmp);
    }
    strcat(request, "\r\n\r\n");

    return strlen(request);
}

int read_file_chunck(short sock, char *buffer, int len)
{
    int size;

    size = recv_eagain(sock, buffer, len, 0, 10);
    if(size <= 0)
        return -1;
    return size;
}

#define RET_IF_ERR(arg, msg, ...) do{ if(arg < 0) { if(sock) sl_Close(sock); UART_PRINT(msg, __VA_ARGS__); return -1; } }while(0)

int load_firmware_http(firmware_info_t *info, const char *server_name, unsigned short port)
{
    int readed;
    long status;
    SlSockAddrIn_t  addr;
    int addr_size;
    short sock = 0;
    unsigned server_ip;
    SlTimeval_t timeout;
    unsigned char buffer[HTTP_BUF_LEN];
    char tmp_path[255];
    unsigned offset = 0;
    unsigned long ulFirmwareToken;
    long lFirmwareFileHandle;
    unsigned crc = 0;
    char *tmp_buffer = buffer;
    int len = 0;

    UART_PRINT("Start firmware loading...\r\n", tmp_path);

    sl_NetAppDnsGetHostByName((_i8 *)server_name, strlen((const char *)server_name), &server_ip, SL_AF_INET);

    addr.sin_family = SL_AF_INET;
    addr.sin_port = sl_Htons(port);
    addr.sin_addr.s_addr = sl_Htonl(server_ip);

    addr_size = sizeof(SlSockAddrIn_t);

    sock = sl_Socket(SL_AF_INET,SL_SOCK_STREAM, 0);
    RET_IF_ERR(sock, "load_firmware_tcp: ERROR Socket Open, status=%d\r\n", sock);

    timeout.tv_sec=2;
    timeout.tv_usec=0;
    status = sl_SetSockOpt(sock, SL_SOL_SOCKET, SL_SO_RCVTIMEO, &timeout, sizeof(SlTimeval_t));
    RET_IF_ERR(status, "load_firmware_tcp: ERROR sl_SetSockOpt, status=%d\r\n", status);

    status = sl_Connect(sock, ( SlSockAddr_t *)&addr, (_u16)addr_size);
    RET_IF_ERR(status, "load_firmware_tcp: ERROR Socket Connect, status=%ld\r\n", status);

    offset = create_request(buffer, "GET", server_name, port, info->filename);
    readed = sl_Send(sock, buffer, offset, 0);
    RET_IF_ERR(readed, "load_firmware_tcp: ERROR Socket Send II, status=%ld\r\n", readed);

    memset(buffer, 0, HTTP_BUF_LEN);
    readed = read_file_chunck(sock, buffer, HTTP_BUF_LEN);

    memset(tmp_path, 0, 255);
    strcpy(tmp_path, "/tmp");
    strcat(tmp_path, info->filename);

    UART_PRINT("filename %s\r\n", tmp_path);

    status = sl_FsOpen(tmp_path, FS_MODE_OPEN_CREATE(info->file_size, _FS_FILE_OPEN_FLAG_COMMIT|_FS_FILE_PUBLIC_WRITE), &ulFirmwareToken, &lFirmwareFileHandle);
    if(status < 0)
    {
        sl_FsClose(lFirmwareFileHandle, 0, 0, 0);
        UART_PRINT("load_firmware_tcp: ERROR in sl_FsOpen, status=%ld\r\n", status);
        return -1;
    }

    offset = 0;
    tmp_buffer = strstr(buffer, "\r\n\r\n");
    tmp_buffer += 4;
    readed -= (int)tmp_buffer - (int)buffer;
    while(info->file_size > offset)
    {
        if(!len)
        {
            char *tmp = strstr(tmp_buffer, "\r\n");

            if(tmp)
            {
                *tmp = 0;
                len = (int)strtol(tmp_buffer, NULL, 16);
                readed -= (int)(tmp + 2) - (int)tmp_buffer;
                tmp_buffer = (tmp + 2);
            }
            else
            {
                readed = (int)tmp_buffer - (int)buffer;
                memmove(buffer, tmp_buffer, readed);
                readed = read_file_chunck(sock, buffer + readed, HTTP_BUF_LEN - readed);
                RET_IF_ERR(readed, "load_firmware_tcp: ERROR Socket Recv II, status=%ld\r\n", readed);
                tmp_buffer = buffer;
                continue;
            }
        }

        if(len && readed > len)
        {
            crc = crc32(crc, tmp_buffer, len);
            status = sl_FsWrite(lFirmwareFileHandle, offset, (_u8*)tmp_buffer, len);
            tmp_buffer += len;
            offset += len;
            readed -= len;
            len = 0;
        }
        else if(len)
        {
            len -= readed;

            crc = crc32(crc, tmp_buffer, readed);
            status = sl_FsWrite(lFirmwareFileHandle, offset, (_u8*)tmp_buffer, readed);
            offset += readed;
            readed = read_file_chunck(sock, buffer, HTTP_BUF_LEN);
            RET_IF_ERR(readed, "load_firmware_tcp: ERROR Socket Recv III, status=%ld\r\n", readed);
            tmp_buffer = buffer;
        }
    }
    UART_PRINT("Firmware loaded\r\n", crc);


    if(lFirmwareFileHandle)
        sl_FsClose(lFirmwareFileHandle, 0, 0, 0);
    sl_Close(sock);

    if(info->hash && info->hash != crc)
        return -1;

    return 0;
}

int write_firmware(firmware_info_t *info)
{
    unsigned long ulReadToken;
    long lReadFileHandle;
    unsigned long ulFirmwareToken;
    long lFirmwareFileHandle;
    long status;
    int ret = -1;
    unsigned char buffer[2 * 1024];
    unsigned buf_len = sizeof(buffer);
    unsigned len_left = info->file_size;
    char tmp_path[255];

    UART_PRINT("Start firmware updating... \r\n");

    memset(tmp_path, 0, 255);
    strcpy(tmp_path, "/tmp");
    strcat(tmp_path, info->filename);

    sl_FsDel(FIRMWARE_FILE_PATH, 0);

    status = sl_FsOpen((unsigned char *)tmp_path, FS_MODE_OPEN_READ, &ulReadToken, &lReadFileHandle);

    if(status < 0)
    {
        sl_FsClose(lReadFileHandle, 0, 0, 0);
        UART_PRINT("check_and_update_firmware I: ERROR in sl_FsOpen, [file %s] status=%ld\r\n", tmp_path, status);
        return ret;
    }    

    status = sl_FsOpen(FIRMWARE_FILE_PATH, FS_MODE_OPEN_CREATE(info->file_size, _FS_FILE_OPEN_FLAG_COMMIT|_FS_FILE_PUBLIC_WRITE), &ulFirmwareToken, &lFirmwareFileHandle);
    if(status < 0)
    {
        sl_FsClose(lReadFileHandle, 0, 0, 0);
        sl_FsClose(lFirmwareFileHandle, 0, 0, 0);
        UART_PRINT("check_and_update_firmware II: ERROR in sl_FsOpen, status=%ld\r\n", status);
        return ret;
    }

    while(len_left > 0)
    {
        status = sl_FsRead(lReadFileHandle, info->file_size - len_left, (_u8 *)buffer, buf_len);

        if(status > 0)
            status = sl_FsWrite(lFirmwareFileHandle, info->file_size - len_left, (_u8 *)buffer, status);

        if (status < 0)
        {
            sl_FsClose(lReadFileHandle, 0, 0, 0);
            sl_FsClose(lFirmwareFileHandle, 0, 0, 0);
            UART_PRINT("_ReadStatFile: ERROR in sl_FsRead, status=%ld\r\n", status);
            return -1;
        }

        len_left -= status;
    }    

    sl_FsClose(lReadFileHandle, 0, 0, 0);
    sl_FsClose(lFirmwareFileHandle, 0, 0, 0);
    sl_FsDel((const _u8*)tmp_path, 0);

    UART_PRINT("Firmware updated\r\n");

    return ret;
}

int update_firmware(const char *server_host, unsigned short server_port, const char *file_path, unsigned checksum, unsigned firmware_size)
{
    firmware_info_t file_info;
    file_info.filename[0] = '/';
    strcpy(file_info.filename+1, file_path);
    file_info.file_size = firmware_size;
    file_info.hash = checksum;
    if(load_firmware_http(&file_info, server_host, server_port) == 0)
        write_firmware(&file_info);
    return -1;
}

firmware_version_t get_firmware_version()
{
    return cur_version;
}
