//
// Created by jshaz on 2018/1/6.
//
#include "com_jshaz_daigo_util_SecurityJni.h"
#include "string.h"

//将char类型转换成jstring类型
jstring CStr2Jstring( JNIEnv* env,const char* str )
{
    jsize len = strlen(str);
    // 定义java String类 strClass
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    //设置String, 保存语言类型,用于byte数组转换至String时的参数
    jstring encoding = (*env)->NewStringUTF(env, "GB2312");
    // 获取java String类方法String(byte[],String)的构造器,用于将本地byte[]数组转换为一个新String
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    // 建立byte数组
    jbyteArray bytes = (*env)->NewByteArray(env, len);
    // 将char* 转换为byte数组
    (*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte*)str);
    //将byte数组转换为java String,并输出
    return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

//将jstring类型转换成char类型
char * Jstring2CStr( JNIEnv * env, jstring jstr )
{
    char * rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "GB2312");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env, jstr,mid,strencode);
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte * ba = (*env)->GetByteArrayElements(env, barr,JNI_FALSE);
    if(alen > 0)
    {
        rtn = (char*)malloc(alen+1); //new char[alen+1];
        memcpy(rtn,ba,alen);
        rtn[alen]=0;
    }
    (*env)->ReleaseByteArrayElements(env, barr,ba,0);

    return rtn;
}

JNIEXPORT jstring JNICALL Java_com_jshaz_daigo_util_SecurityJni_getSL
  (JNIEnv *env, jclass jobj, jstring name) {
  	char* dst = "http://111.231.133.230:8080/daigo/";
  	char* sl;
  	strncpy(sl, Jstring2CStr(env, name), 2);
  	strcat(dst, sl);
    return (*env)->NewStringUTF(env, dst);
}
