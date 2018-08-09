# RecorderUtil

拍照jpg, 录像mp4, 录音 amr  ,使用Android Studio3.1.3纯kotlin 1.2.60编写

```
    private var cameraSurfaceView: CameraSurfaceView? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //初始化页面
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        //supportActionBar!!.hide()
        infoto()//打开视频录像，拍照

    }

    fun infoto() {
        cameraSurfaceView = findViewById<CameraSurfaceView>(R.id.cameraSurfaceView)
        //拍照
        findViewById<View>(R.id.capture).setOnClickListener {
                cameraSurfaceView!!.capture()

        }
        (findViewById<View>(R.id.luxiang) as ToggleButton).setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                cameraSurfaceView!!.startRecord()//开始录像
                //设置录制时长为10秒视频
                //                    cameraSurfaceView.startRecord(10000, new MediaRecorder.OnInfoListener() {
                //                        @Override
                //                        public void onInfo(MediaRecorder mr, int what, int extra) {
                //                            cameraSurfaceView.stopRecord();
                //                            buttonView.setChecked(false);
                //                        }
                //                    });
            } else
                cameraSurfaceView!!.stopRecord()//结束录像
        }
        //打开灯光，关闭灯光
        (findViewById<View>(R.id.light) as ToggleButton).setOnCheckedChangeListener { buttonView, isChecked -> cameraSurfaceView!!.switchLight(isChecked) }

        //录音
        (findViewById<View>(R.id.luyin) as ToggleButton).setOnCheckedChangeListener { buttonView,
                                                                                          isChecked ->
            if (isChecked) {
                voiceline.visibility = View.VISIBLE
                recorder = RecorderUtil()
                recorder!!.startRecording() //开始录音
            } else {
                voiceline.visibility = View.GONE
                recorder!!.stopRecording()//结束录音

            }
        }

    }

    var recorder:RecorderUtil?=null
    override fun onDestroy() {
        super.onDestroy()
        //关闭camera
        cameraSurfaceView!!.closeCamera()
    }
```
