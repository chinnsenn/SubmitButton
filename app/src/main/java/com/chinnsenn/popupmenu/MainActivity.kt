package com.chinnsenn.popupmenu

import android.animation.ValueAnimator
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chinnsenn.submitbutton.SubmitButton

class MainActivity : AppCompatActivity() {

    private lateinit var submitButton: SubmitButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
//                submitButton.failure()
//            } else {
                submitButton.setProgress(percent)
//            }
        }

        submitButton.setOnStatusListener(object : SubmitButton.OnStatusListener {

            override fun onLoad() {
                animator.start()
            }

            override fun onComplete() {

            }

            override fun onCancel() {

            }

            override fun onStart() {
                submitButton.postDelayed({ submitButton.stop() }, 15000)
            }
        })
    }
}
