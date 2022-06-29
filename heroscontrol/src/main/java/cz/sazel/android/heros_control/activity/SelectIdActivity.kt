package cz.sazel.android.heros_control.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import cz.sazel.android.heros_control.Constants.ID
import cz.sazel.android.heros_control.Constants.IP
import cz.sazel.android.heros_control.Constants.NAME
import cz.sazel.android.heros_control.Id
import cz.sazel.android.heros_control.R
import cz.sazel.android.heros_control.databinding.SelectIdBinding
import cz.sazel.android.heros_control.viewmodel.SelectIdVM

/**
 * Created by wojta on 16.5.14.
 */
class SelectIdActivity : FragmentActivity() {

    private val viewModel by lazy { ViewModelProvider(this)[SelectIdVM::class.java] }
    private lateinit var binding: SelectIdBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SelectIdBinding.inflate(layoutInflater)
        setContentView(binding.root)
        with(binding) {
            lvList.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val (id1, name, isIp) = parent.adapter.getItem(position) as Id
                val intent = Intent()
                intent.putExtra(ID, id1)
                intent.putExtra(NAME, name)
                intent.putExtra(IP, isIp)
                setResult(RESULT_OK, intent)
                finish()
            }
            viewModel.load()
            viewModel.idsLiveData.observe(this@SelectIdActivity, Observer {
                lvList.adapter = ArrayAdapter(this@SelectIdActivity, android.R.layout.simple_list_item_1, android.R.id.text1, it)
            })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_addip) {
            showEnterIpDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showEnterIpDialog() {
        val builder = AlertDialog.Builder(this)
        val edIp = EditText(this)
        builder.setView(edIp).setMessage(getString(R.string.enter_ip)).setPositiveButton(R.string.ok) { dialog, which ->
            val text = edIp.text.toString()
            val list = viewModel.idsLiveData.value
            list?.add(Id(text, text, true))
            viewModel.idsLiveData.postValue(list)
            (binding.lvList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
        }
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.select_id, menu)
        return super.onCreateOptionsMenu(menu)
    }

}