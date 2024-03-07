//
//import android.app.AlertDialog
//import android.content.Context
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.os.Message
//import com.wordle.client.util.ProgressDialog
//import okhttp3.*
//import java.io.IOException
//
///**
// * Asynchronous calls to server api helper classes
// * Created by Zhiqiang Liu
// */
//open class OkHttpUtil {
//
//    // OKHttp instance
//    val client = OkHttpClient()
//
//    var mContext: Context? = null
//
//    // Static Variable
//    companion object {
//        // The base server url
//        const val BASE_URL = "http://10.0.2.2:5000/"
//
//        // Bundle message
//        const val MSG = "msg"
//        const val TITLE = "title"
//        const val DEFAULT_MSG = 0x0
//        const val ALL_USER_MSG = 0x1
//        const val LOGIN_MSG = 0x2
//        const val SERVER_ERROR="Server Error"
//        const val PROGRESS_DIALOG_SHOW = 0x11
//        const val PROGRESS_DIALOG_CLOSE = 0x12
//        const val ALERT_DIALOG_SHOW = 0x13
//    }
//
//    /**
//     * This handler handle the dialog show and close
//     *
//     */
//    var dialogHandler = object : Handler(Looper.getMainLooper()) {
//
//        // Progress Dialog
//        var progressDialog: ProgressDialog? =null
//
//        fun openProgressDialog(){
//            if(progressDialog == null) {
//                progressDialog = ProgressDialog()
//            }
//            // non null call
//            progressDialog!!.showProgress(mContext)
//        }
//
//        fun closeProgressDialog(){
//            progressDialog?.closeProgress()
//        }
//
//        fun showMessage(title:String, msg:String){
//            var dialog = AlertDialog.Builder(mContext)
//            dialog.create()
//            dialog.setMessage(msg)
//            dialog.setTitle(title)
//            dialog.show()
//        }
//
//        override fun handleMessage(msg: Message) {
//            super.handleMessage(msg)
//            when(msg?.what){
//                PROGRESS_DIALOG_SHOW->{
//                    openProgressDialog()
//                }
//                PROGRESS_DIALOG_CLOSE->{
//                    closeProgressDialog()
//                }
//                ALERT_DIALOG_SHOW->{
//                    var title = msg.data.getString(TITLE)
//                    var message = msg.data.getString(MSG)
//                    if (title != null && message!=null) {
//                        showMessage(title, message)
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * To receive the message
//     * You must implement a handler in the activity, and pass it in.
//     */
//    open fun notifyActivity(mHandler: Handler, mBundle: Bundle?, what: Int){
//        mHandler.sendMessage(createMessage(mBundle,what))
//    }
//
//    /**
//     * Create a Message and return
//     */
//    open fun createMessage(mBundle: Bundle? , what: Int): Message {
//        val mMessage = Message()
//        mMessage.data = mBundle
//        mMessage.what = what
//        return mMessage
//    }
//
//    /**
//     * Send a message to dialog handler
//     */
//    open fun sendMessage(mBundle: Bundle?, what:Int){
//        dialogHandler?.sendMessage(createMessage(mBundle, what))
//    }
//
//    /**
//     * Send message to show ProgressDialog
//     */
//    open fun sendProgressDialogShowMessage() {
//        sendMessage(Bundle(), PROGRESS_DIALOG_SHOW)
//    }
//
//    /**
//     * Send message to close ProgressDialog
//     */
//    open fun sendProgressDialogCloseMessage() {
//        sendMessage(Bundle(), PROGRESS_DIALOG_CLOSE)
//    }
//
//    /**
//     * Send message to show AlertDialog
//     */
//    open fun sendMessage(message: String, title: String) {
//        val mBundle = Bundle()
//        mBundle.putString(MSG, message)
//        mBundle.putString(TITLE, title)
//        sendMessage(mBundle, ALERT_DIALOG_SHOW)
//    }
//
//
//    /**
//     * Get request
//     * Asynchronous calls to server api
//     * func, the api
//     * handleMsg, the message type
//     * handler, the handler that deal with the data returned by server.
//     */
//    open fun get(func: String, handleMsg: Int, handler: Handler, mContext:Context) {
//        this.mContext = mContext
//        sendProgressDialogShowMessage()
//        val request = Request.Builder()
//            .url(BASE_URL + func)
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                sendProgressDialogCloseMessage()
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    sendProgressDialogCloseMessage()
//
//                    if (!response.isSuccessful) {
//                        sendMessage("Cannot connect to the local web server.",SERVER_ERROR)
//                        throw IOException()
//                    }
//                    var data = response.body!!.string()
//
//                    var bundle = Bundle()
//                    bundle.putString(MSG, data)
//                    notifyActivity(mHandler = handler, bundle, ALL_USER_MSG)
//                }
//            }
//        })
//    }
//
//    /**
//     * Post request
//     * Asynchronous calls to server api
//     * func, the api
//     * handleMsg, the message type
//     * handler, the handler that deal with the data returned by server.
//     */
//    open fun login(
//        username: String,
//        password: String,
//        handler: Handler,
//        mContext: Context
//    ) {
//        this.mContext = mContext
//        val builder = FormBody.Builder()
//        builder.add("username", username)
//        builder.add("password", password)
//        post("login", builder, handler, ALL_USER_MSG)
//    }
//
//    open fun post(
//        func: String,
//        params:FormBody.Builder,
//        handler: Handler,
//        what: Int
//    ) {
//
//        sendProgressDialogShowMessage()
//        val formBody = params.build()
//        val request = Request.Builder()
//            .url(BASE_URL + func)
//            .post(formBody)
//            .build()
//        client.newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                sendProgressDialogCloseMessage()
//                e.printStackTrace()
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                response.use {
//                    sendProgressDialogCloseMessage()
//                    if (response.code == 401) {
//                        sendMessage("Incorrect user or password input", "Login fail")
//                    }
//                    else if (!response.isSuccessful) {
//                        sendMessage("Cannot connect to the local web server.",SERVER_ERROR)
//                        throw IOException()
//                    }
//                    var data = response.body!!.string()
//                    val mBundle = Bundle()
//                    mBundle.putString(MSG, data)
//                    notifyActivity(handler, mBundle, what)
//                }
//            }
//        })
//    }
//}