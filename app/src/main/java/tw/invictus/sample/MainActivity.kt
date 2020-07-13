package tw.invictus.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import tw.invictus.sample.simple.DraggableFrameDemoActivity
import tw.invictus.sample.simple.SimpleVideoListActivity

class MainActivity : AppCompatActivity() {

    private lateinit var scalableFrameDemo: Button
    private lateinit var draggableFrameDemo: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scalableFrameDemo = findViewById<Button>(R.id.scalable_frame_demo).apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, SimpleVideoListActivity::class.java)
                startActivity(intent)
            }
        }
        draggableFrameDemo = findViewById<Button>(R.id.draggable_frame_demo).apply {
            setOnClickListener {
                val intent = Intent(this@MainActivity, DraggableFrameDemoActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scalableFrameDemo.setOnClickListener(null)
        draggableFrameDemo.setOnClickListener(null)
    }
}