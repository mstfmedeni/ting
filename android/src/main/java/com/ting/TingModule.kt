package com.ting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.hjq.window.EasyWindow
import com.hjq.window.draggable.SpringDraggable
import java.io.IOException
import java.net.URL


class TingModule internal constructor(context: ReactApplicationContext) :
  TingSpec(context) {

  private var context: Context = context
  private var alertWindow: EasyWindow<EasyWindow<*>>? = null

  @ReactMethod
  override fun toast(options: ReadableMap) {
    val easyWindow = EasyWindow<EasyWindow<*>>(currentActivity)
    val inflater = LayoutInflater.from(context)
    val container = inflater.inflate(
      R.layout.toast, null
    ) as LinearLayout

    // init view
    val messageView: TextView = container.findViewById(R.id.message)
    val titleView: TextView = container.findViewById(R.id.title)
    val iconView: ImageView = container.findViewById(R.id.icon)
    val layoutView: LinearLayout = container.findViewById(R.id.layoutView)

    // get option from js
    val title = options?.getString("title")
    val message = options?.getString("message")
    val icon = options?.getMap("icon") as? ReadableMap
    val iconURI = icon?.getString("uri")
    val preset = options?.getString("preset")
    val duration = options?.getInt("duration")
    val time: Int = if (duration != null) duration * 1000 else 3000
    var toastAnim = R.style.ToastTopAnim
    val position: Int = when (options?.getString("position")) {
      "bottom" -> {
        toastAnim = R.style.ToastBottomAnim
        Gravity.BOTTOM
      }

      else -> {
        Gravity.TOP
      }
    }

    //set title
    titleView.text = title

    // check message
    if (message == null) {
      messageView.visibility = TextView.GONE
    } else {
      messageView.text = message
    }
    if (iconURI == null) {
      when (preset) {
        "done" -> {
          loadDoneIcon(iconView)
        }

        "error" -> {
          iconView.setImageResource(R.drawable.close)
        }

        "none" -> {
          iconView.visibility = ImageView.GONE
        }

        else -> null
      }
    } else {
      val bitmap = loadBitmapFromUri(iconURI)
      if (bitmap != null) {
        iconView.setImageBitmap(bitmap)
      } else {
        loadDoneIcon(iconView)
      }
    }

    runOnUiThread {
      easyWindow.apply {
        setDuration(time)
        contentView = container
        setGravity(position)
        setYOffset(48)
        draggable = SpringDraggable(SpringDraggable.ORIENTATION_VERTICAL)
        setAnimStyle(toastAnim)
        setOutsideTouchable(true)
        setOnClickListener(
          R.id.toast,
          EasyWindow.OnClickListener<LinearLayout> { toast: EasyWindow<*>, _: LinearLayout ->
            toast.cancel()
          })
      }.show()
    }
  }

  @ReactMethod
  override fun alert(options: ReadableMap) {
//
  }

  @ReactMethod
  override fun dismissAlert() {
    if (alertWindow != null && alertWindow!!.isShowing) {
      runOnUiThread {
        alertWindow!!.cancel()
      }
    }
  }

  override fun getName(): String {
    return NAME
  }

  companion object {
    const val NAME = "Ting"
  }

  private fun loadDoneIcon(iconView: ImageView) {
    iconView.setImageResource(R.drawable.done)
    val avd = iconView.drawable as AnimatedVectorDrawable
    val handler = Handler(Looper.getMainLooper())
    handler.postDelayed({
      avd.start()
    }, 300)
  }


}

fun loadBitmapFromUri(imageUri: String): Bitmap? {
  var bitmap: Bitmap? = null
  try {
    val url = URL(imageUri)
    val connection = url.openConnection()
    connection.connect()
    val inputStream = connection.getInputStream()
    bitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
  } catch (e: IOException) {
    e.printStackTrace()
  }
  return bitmap
}
