#include "com_laojiang_myaudioprise_util_MixRecord.h"
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/ioctl.h>
//#include <sys/soundcard.h>
#include "mad.h"
#include <jni.h>
#include <android/log.h>
#define LOG_TAG   "libmad"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define BUFSIZE 8192

/*
 * This is a private message structure. A generic pointer to this structure
 * is passed to each of the callback functions. Put here any data you need
 * to access from within the callbacks.
 */
struct buffer {
    FILE *fp; /*file pointer*/
    unsigned int flen; /*file length*/
    unsigned int fpos; /*current position*/
    unsigned char fbuf[BUFSIZE]; /*buffer*/
    unsigned int fbsize; /*indeed size of buffer*/
};
typedef struct buffer mp3_file;

int soundfd; /*soundcard file*/
unsigned int prerate = 0; /*the pre simple rate*/

int writedsp(int c)
{
    return write(soundfd, (char *)&c, 1);
}

void set_dsp(char const* fp )
{
#if 0
    int format = AFMT_S16_LE; //PCM 16λÿ������
//    int channels = 2;
    int channels = 1; //������
    int rate = 44100; //������

    soundfd = open(fp, O_WRONLY);
    ioctl(soundfd, SNDCTL_DSP_SPEED,&rate);
    ioctl(soundfd, SNDCTL_DSP_SETFMT, &format);
    ioctl(soundfd, SNDCTL_DSP_CHANNELS, &channels);
#else
    if((soundfd = open(fp , O_WRONLY | O_CREAT)) < 0)
    {
        fprintf(stderr , "can't open sound device!\n");
        exit(-1);
    }
#endif
}

/*
 * This is perhaps the simplest example use of the MAD high-level API.
 * Standard input is mapped into memory via mmap(), then the high-level API
 * is invoked with three callbacks: input, output, and error. The output
 * callback converts MAD's high-resolution PCM samples to 16 bits, then
 * writes them to standard output in little-endian, stereo-interleaved
 * format.
 */

static int decode(mp3_file *mp3fp);

static char* Jstring2CStr(JNIEnv* env, jstring jstr);

JNIEXPORT jint JNICALL Java_com_laojiang_myaudioprise_util_MixRecord_decodeMp3ToPCM2(
		JNIEnv *env, jobject obj, jstring mp3File, jstring mp3PCM) {

	char const* filemp3 = Jstring2CStr(env, mp3File);
	char const* filemp3PCM = Jstring2CStr(env, mp3PCM);

	LOGI("samplerate=%s", filemp3);
	LOGI("samplerate=%s", filemp3PCM);

    long flen, fsta, fend;
    int dlen;
    mp3_file *mp3fp;

    mp3fp = (mp3_file *)malloc(sizeof(mp3_file));
    if((mp3fp->fp = fopen(filemp3, "r")) == NULL)
    {
        printf("can't open source file.\n");
        return 2;
    }
    //���ص�ǰ�ļ�ָ���λ�á����λ����ָ��ǰ�ļ�ָ��������ļ���ͷ��λ������
    fsta = ftell(mp3fp->fp);
    //ת���ļ���β
    fseek(mp3fp->fp, 0, SEEK_END);
    //���ص�ǰ�ļ�ָ���λ�á����λ����ָ��ǰ�ļ�ָ��������ļ���ͷ��λ������
    fend = ftell(mp3fp->fp);
    //����ļ���βλ��
    flen = fend - fsta;
    if(flen > 0)
        fseek(mp3fp->fp, 0, SEEK_SET);
    fread(mp3fp->fbuf, 1, BUFSIZE, mp3fp->fp);
    mp3fp->fbsize = BUFSIZE;
    mp3fp->fpos = BUFSIZE;
    mp3fp->flen = flen;

    set_dsp(filemp3PCM);

    decode(mp3fp);

    close(soundfd);
    fclose(mp3fp->fp);

    return 0;
}



char* Jstring2CStr(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;  //rtn һ���ܹ����char�����ڴ���׵�ַ
	// �ҵ� jvm���� String��
	jclass clsstring = (*env)->FindClass(env, "java/lang/String");
	// ����һ��jstring���ַ������� GB2312
	jstring strencode = (*env)->NewStringUTF(env, "UTF-8");
	// Ѱ�� String���һ������ getBytes   getBytes�����Ĳ��� Ljava/lang/String;
	// getBytes �����ķ���ֵ [B
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes",
			"(Ljava/lang/String;)[B");
	// "String".getBytes("GB2312");
	jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid,
			strencode);
	//��ȡ��barr�ĳ���
	jsize alen = (*env)->GetArrayLength(env, barr);
	//��ȡ����jbyte ���ݷ��뵽��ba Ϊ��ʼ��ַ���ڴ�ռ���
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1);        //new   char[alen+1]; "\0" �ַ����Ľ���
		//�ڴ濽���Ĳ��� ,
		memcpy(rtn, ba, alen);
		//���ַ��������һ��Ԫ��  ��ʾ�ַ�������
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0);
	return rtn;
}

static enum mad_flow input(void *data, struct mad_stream *stream)
{
    mp3_file *mp3fp;
    int ret_code;
    int unproc_data_size; /*the unprocessed data's size*/
    int copy_size;

    mp3fp = (mp3_file *)data;
    if(mp3fp->fpos < mp3fp->flen) {
        unproc_data_size = stream->bufend - stream->next_frame;
        //printf("%d, %d, %d\n", unproc_data_size, mp3fp->fpos, mp3fp->fbsize);
        memcpy(mp3fp->fbuf, mp3fp->fbuf + mp3fp->fbsize - unproc_data_size, unproc_data_size);
        copy_size = BUFSIZE - unproc_data_size;
        if(mp3fp->fpos + copy_size > mp3fp->flen) {
            copy_size = mp3fp->flen - mp3fp->fpos;
        }
        fread(mp3fp->fbuf+unproc_data_size, 1, copy_size, mp3fp->fp);
        mp3fp->fbsize = unproc_data_size + copy_size;
        mp3fp->fpos += copy_size;

        /*Hand off the buffer to the mp3 input stream*/
        mad_stream_buffer(stream, mp3fp->fbuf, mp3fp->fbsize);
        ret_code = MAD_FLOW_CONTINUE;
    } else {
        ret_code = MAD_FLOW_STOP;
    }

    return ret_code;

}

//static enum mad_flow header(void *data, struct mad_header const *header)
//{
//
//	unsigned int samplerate = header->samplerate;
//
//	LOGI( "samplerate123:%d", samplerate);
//
//    return MAD_FLOW_CONTINUE;
//
//}

/*
 * The following utility routine performs simple rounding, clipping, and
 * scaling of MAD's high-resolution samples down to 16 bits. It does not
 * perform any dithering or noise shaping, which would be recommended to
 * obtain any exceptional audio quality. It is therefore not recommended to
 * use this routine if high-quality output is desired.
 */

static inline signed int scale(mad_fixed_t sample)
{
    /* round */
    sample += (1L << (MAD_F_FRACBITS - 16));

    /* clip */
    if (sample >= MAD_F_ONE)
        sample = MAD_F_ONE - 1;
    else if (sample < -MAD_F_ONE)
        sample = -MAD_F_ONE;

    /* quantize */
    return sample >> (MAD_F_FRACBITS + 1 - 16);
}

/*
 * This is the output callback function. It is called after each frame of
 * MPEG audio data has been completely decoded. The purpose of this callback
 * is to output (or play) the decoded PCM audio.
 * �߳���
 * ----------
 * 	¼��
 * 	----------
 * 		------
 * 			����
 * 			�峪
 * 			����¼�� ���� ���� ����
 * 		-----
 * ----------
 * ����
 */

//�����������Ӧ���޸ģ�Ŀ���ǽ����������ʱ�����������⡣
static enum mad_flow output(void *data, struct mad_header const *header,
        struct mad_pcm *pcm)
{
    unsigned int nchannels, nsamples,samplerate;
    mad_fixed_t const *left_ch, *right_ch;
    // pcm->samplerate contains the sampling frequency
    nchannels = pcm->channels;
    nsamples = pcm->length;
    left_ch = pcm->samples[0];
    right_ch = pcm->samples[1];
    short buf[nsamples *2];
    samplerate = pcm->samplerate;
    LOGI("samplerate=%d", samplerate);
    LOGI("nchannels=%d", nchannels);
    int i = 0;
    //printf(">>%d\n", nsamples);
    while (nsamples--) {
        signed int sample;
        // output sample(s) in 16-bit signed little-endian PCM

        sample = scale(*left_ch++);
        buf[i++] = sample & 0xFFFF;

        if (nchannels == 2 && samplerate!=44100) {
            sample = scale(*right_ch++);
            buf[i++] = sample & 0xFFFF;
        }
    }
    //fprintf(stderr, ".");
    write(soundfd, &buf[0], i * 2);
    return MAD_FLOW_CONTINUE;
}

/*
 * This is the error callback function. It is called whenever a decoding
 * error occurs. The error is indicated by stream->error; the list of
 * possible MAD_ERROR_* errors can be found in the mad.h (or stream.h)
 * header file.
 */

static enum mad_flow error(void *data,
        struct mad_stream *stream,
        struct mad_frame *frame)
{
    mp3_file *mp3fp = data;

    fprintf(stderr, "decoding error 0x%04x (%s) at byte offset %u\n",
            stream->error, mad_stream_errorstr(stream),
            stream->this_frame - mp3fp->fbuf);

    /* return MAD_FLOW_BREAK here to stop decoding (and propagate an error) */

    return MAD_FLOW_CONTINUE;
}

/*
 * This is the function called by main() above to perform all the decoding.
 * It instantiates a decoder object and configures it with the input,
 * output, and error callback functions above. A single call to
 * mad_decoder_run() continues until a callback function returns
 * MAD_FLOW_STOP (to stop decoding) or MAD_FLOW_BREAK (to stop decoding and
 * signal an error).
 */

static int decode(mp3_file *mp3fp)
{
    struct mad_decoder decoder;
    int result;

    /* configure input, output, and error functions */
    mad_decoder_init(&decoder, mp3fp,
            input, 0 /* header */, 0 /* filter */, output,
            error, 0 /* message */);
    /* start decoding */
    result = mad_decoder_run(&decoder, MAD_DECODER_MODE_SYNC);

    /* release the decoder */
    mad_decoder_finish(&decoder);

    return result;
}
JNIEXPORT jint JNICALL Java_com_laojiang_myaudioprise_util_MixRecord_mix2PCMToPCM(
		JNIEnv *env, jobject obj, jstring sourcePCM, jstring mp3PCM,
		jstring mixPCM) {
	char const* filesourcePCM = (*env)->GetStringUTFChars(env, sourcePCM, 0);
	char const* filemp3PCM = (*env)->GetStringUTFChars(env, mp3PCM, 0);
	char const* filemixPCM = (*env)->GetStringUTFChars(env, mixPCM, 0);

	FILE *pcm1, *pcm2, *mix;
	char sample1, sample2;
	int value;
	pcm1 = fopen(filesourcePCM, "r");
	pcm2 = fopen(filemp3PCM, "r");
	mix = fopen(filemixPCM, "w");
	while (!feof(pcm1)) {
		sample1 = fgetc(pcm1);
		sample2 = fgetc(pcm2);
		if ((sample1 < 0) && (sample2 < 0)) {
			value = sample1 + sample2
					- (sample1 * sample2 / -(pow(2, 16 - 1) - 1));
		} else {
			value = sample1 + sample2
					- (sample1 * sample2 / (pow(2, 16 - 1) - 1));
		}
		fputc(value, mix);
	}
	fclose(pcm1);
	fclose(pcm2);
	fclose(mix);
	return 0;
}
void Mix(char sourseFile[10][SIZE_AUDIO_FRAME],int number,char *objectFile)
{
    //��һ������
    int const MAX=32767;
    int const MIN=-32768;

    double f=1;
    int output;
    int i = 0,j = 0;
    for (i=0;i<SIZE_AUDIO_FRAME/2;i++)
    {
        int temp=0;
        for (j=0;j<number;j++)
        {
            temp+=*(short*)(sourseFile[j]+i*2);
        }
        output=(int)(temp*f);
        if (output>MAX)
        {
            f=(double)MAX/(double)(output);
            output=MAX;
        }
        if (output<MIN)
        {
            f=(double)MIN/(double)(output);
            output=MIN;
        }
        if (f<1)
        {
            f+=((double)1-f)/(double)32;
        }
        *(short*)(objectFile+i*2)=(short)output;
    }
}
int printWriteCount(JNIEnv *env, jobject obj,int writeCount){
	//1.�ҵ�java����native�������ڵ��ֽ����ļ�
    //jclass (*FindClass)(JNIEnv*, const char*);
    jclass clazz = (*env)->FindClass(env, "com/qd/videorecorder/MP3AndMP3MIX");
    if(clazz == 0){
        LOGE("find class error");
        return 0;
    }
    LOGE("find class");
    //2.�ҵ�class�����Ӧ�ķ���
    // jmethodID (*GetMethodID)(JNIEnv*, jclass, const char*, const char*);
    jmethodID method2 = (*env)->GetMethodID(env,clazz,"writeCount","(I)I");
    if(method2 == 0){
        LOGE("find method2 error");
        return 0;
    }
    LOGE("find method2");
    //3.���÷���
    //jint (*CallIntMethod)(JNIEnv*, jobject, jmethodID, ...);
    return (*env)->CallIntMethod(env, obj, method2, writeCount);
}
