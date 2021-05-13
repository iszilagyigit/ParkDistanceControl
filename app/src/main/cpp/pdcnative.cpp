//
// Created by Istvan Szilagyi on 03.04.21.
//
#include <jni.h>
#include <string>
#include <sys/ioctl.h>
#include <errno.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <linux/spi/spidev.h>
#include <android/log.h>


static const char *device = "/dev/spidev0.0";
static uint32_t speed = 500000;
static uint32_t mode;

static void transfer(int fd, uint32_t const *tx, uint32_t *rx, size_t len)
{
    int ret;
    struct spi_ioc_transfer tr = {
            .tx_buf = (unsigned long)tx,
            .rx_buf = (unsigned long)rx,
            .len = (unsigned int) len,
            .speed_hz = speed,
            .bits_per_word = 8,
            .tx_nbits = 8,
            .rx_nbits = 8,

    };

    ret = ioctl(fd, SPI_IOC_MESSAGE(1), &tr);
    if (ret < 1) {
        __android_log_print(ANDROID_LOG_INFO, "SPI","can't send spi message");
    }
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_pdc_main_parkdistancecontrol2_ParkSensorBackgroundService_initSPIDevice(
        JNIEnv *env,
        jobject /* this */)  {
    int fd = open(device, O_RDWR);
    if (fd < 0) {
        __android_log_print(ANDROID_LOG_ERROR,"SPI", "can't open device. ERR: %d", errno);
        if (errno == EACCES) {
            __android_log_write(ANDROID_LOG_ERROR,"SPI","permission denied");
        }
        return -1;
    }

    /*
     * spi mode
     */
    int ret = ioctl(fd, SPI_IOC_WR_MODE32, &mode);
    if (ret == -1) {
        __android_log_write(ANDROID_LOG_ERROR,"SPI","can't set spi mode");
        return -1;
    }

    ret = ioctl(fd, SPI_IOC_RD_MODE32, &mode);
    if (ret == -1) {
        __android_log_write(ANDROID_LOG_ERROR,"SPI","can't get spi mode");
        return -1;
    }

    /*
     * max speed hz
     */
    ret = ioctl(fd, SPI_IOC_WR_MAX_SPEED_HZ, &speed);
    if (ret == -1) {
        __android_log_write(ANDROID_LOG_ERROR,"SPI","can't set max speed hz");
        return -1;
    }

    ret = ioctl(fd, SPI_IOC_RD_MAX_SPEED_HZ, &speed);
    if (ret == -1) {
        __android_log_write(ANDROID_LOG_ERROR,"SPI","can't get max speed hz");
        return -1;
    }

    __android_log_print(ANDROID_LOG_INFO, "SPI", "spi file handler: %d\n", fd);
    __android_log_print(ANDROID_LOG_INFO, "SPI", "spi mode: 0x%x\n", mode);
    __android_log_print(ANDROID_LOG_INFO, "SPI","max speed: %u Hz (%u kHz)\n", speed, speed/1000);

    return fd;
}

extern "C"
JNIEXPORT jlong JNICALL
Java_com_pdc_main_parkdistancecontrol2_ParkSensorBackgroundService_parkSensorSpi4Bytes(
        JNIEnv *env,
        jobject /* this */,
        jint fd) {

    __android_log_print(ANDROID_LOG_INFO, "SPI", "spi dev handler param: %d\n", fd);

    uint32_t anyValue = 0x01020304;
    uint32_t recBuf = 0x00000000;
    transfer(fd, &anyValue, &recBuf, 4);
    return recBuf;
}

