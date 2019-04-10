package com.chinnsenn.popupmenu

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chinnsenn.submitbutton.SubmitButton

class MainActivity : AppCompatActivity() {

    private lateinit var submitButton: SubmitButton
//    private lateinit var scheduleBar: ScheduleBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        scheduleBar = findViewById(R.id.sb_growup)
//        scheduleBar.totalValue = 2000
//        scheduleBar.startValue = 0
//        scheduleBar.currentProcess = 0

        submitButton = findViewById(R.id.submitbutton)
        submitButton.submitText = "开始上传"
        submitButton.completeText = "上传完成"
        submitButton.failureText = "上传失败"
        submitButton.unKnownProgress = false

        val animator: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 3000
        animator.addUpdateListener {
            var percent = it.animatedValue as Float
//            if (percent > 0.5f) {
//                submitButton.error()
//            } else {
//            println("percent = $percent")
            submitButton.setProgress(percent)
//            }
        }

        submitButton.setOnStatusListener(object : SubmitButton.OnStatusListener {

            override fun onLoad() {
                animator.start()
            }

            override fun onComplete() {
                println("MainActivity.onComplete")
            }

            override fun onCancel() {

            }

            override fun onStart() {

            }
        })
    }
}
