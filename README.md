![build](https://img.shields.io/badge/build-success-blue.svg)
[![GitHub release](https://img.shields.io/badge/release-0.1.0-green.svg)](https://github.com/foreveronly/SubmitButton/raw/master/submitbutton/build/outputs/aar/submitbutton-release.aar)
![](https://img.shields.io/badge/language-kotlin-orange.svg)
# 自定义带进度圈的按钮 --- SubmitButton (kotlin)
![Android](https://ws2.sinaimg.cn/large/006tNbRwly1fwscfx487hg30mi05ktow.gif)

## 使用方法
1、在 gradle 里引用:

`implementation 'com.chinnsenn.submitbutton:submitbutton:0.1.0'`

2、
- 在 xml 中添加控件

```
<com.chinnsenn.submitbutton.SubmitButton
        android:id="@+id/submitbutton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:buttonColor="#11aa03"
        app:buttonStrokeWidth="15"
        app:buttonTextSize="15sp"
        app:completeText="上传完成"
        app:failureText="上传失败"
        app:progressColor="#6377ff"
        app:unKnownProgress = "true"
        app:submitText="确认" />
```
- 属性也可以在代码中设置


```
        val submitButton:SubmitButton = findViewById(R.id.submitbutton)
        submitButton.submitText = "开始上传"
        submitButton.completeText = "上传完成"
        submitButton.failureText = "上传失败"
        submitButton.unKnownProgress = false
```
- 属性介绍

| 属性名 | 描述 |
| --- | --- |
| buttonColor | 按钮颜色 |
| buttonStrokeWidth | 线框宽度 |
| buttonTextSize | 按钮文字大小 |
| completeText | 完成文案 |
| failureText | 失败文案 |
| progressColor | 进度条颜色 |
| submitText | 起始文字 |
| unKnownProgress | 不能确定进度 |

## 两种进度条
- 不确定进度（默认）

`submitButton.unKnownProgress = true`

这种情况下需要手动调用 SubmitButton#stop() 方法结束转进度

-------

- 可以确定进度

**不需要调用 SubmitButton#stop()**

`submitButton.unKnownProgress = false`

但需要在你网络请求进度中或者其他地方调用 

```SubmitButton#setProgress(percent:Float) //需要你计算好百分比``` 

或者

```SubmitButton#setProgressAndTotal(progress: Float, total: Float) //传入当前数值和总进度```
否则进度圈不会变化。


## 失败状态
在网络请求失败的回调里调用 SubmitButton#failure()
![](https://ws4.sinaimg.cn/large/006tNbRwgy1fwvyecvoq9g30mi05kk9f.gif)


此控件为还原 Dribble 上一个动效而制作
![](https://ws2.sinaimg.cn/large/006tNbRwly1fwscfvvrkgg30mi05kdsm.gif)

## 0.2.0更新
- 新增具体进度数字（确定进度状态下）

-------
因为没有具体参数，只能摸索的仿造，也是为了熟悉 kotlin，有许多不足之处，也欢迎PQ。
