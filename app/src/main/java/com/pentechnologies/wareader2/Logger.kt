package com.pentechnologies.wareader2
import kotlinx.android.synthetic.main.activity_main.*

class Logger private constructor(private val mainActivity: MainActivity, private val message: String, private val code: Int?){

    private fun display() {
        if (code != null) {
        //    mainActivity.log_message.text = "$message: error($code)"
        } else {
          //  mainActivity.log_message.text = message
        }
    }
    companion object {
        private var INSTANCE: Logger? = null
        fun log(mainActivity: MainActivity, message: String, code: Int?) {
            if (INSTANCE != null) {
                INSTANCE = null
            }
            INSTANCE = Logger(mainActivity, message, code)
            INSTANCE?.display()
        }
    }
}