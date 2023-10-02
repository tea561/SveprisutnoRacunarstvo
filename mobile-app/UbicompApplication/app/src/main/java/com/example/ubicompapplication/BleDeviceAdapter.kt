package com.example.ubicompapplication

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BleDeviceAdapter(private val items: List<ScanResult>, private val onClickListener: ((device: ScanResult) -> Unit)): RecyclerView.Adapter<BleDeviceAdapter.DeviceViewHolder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleDeviceAdapter.DeviceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.scan_row, parent, false)
        return DeviceViewHolder(view, mListener)
    }

    @SuppressLint("MissingPermission")
    override fun onBindViewHolder(holder: BleDeviceAdapter.DeviceViewHolder, position: Int) {
        if(items[position].device.name.isNullOrEmpty()) {
            holder.deviceName.text = "---"
        } else {
            holder.deviceName.text = items[position].device.name
        }
        holder.macAddress.text = items[position].device.address
        holder.signalStrength.text = "${items[position].rssi} dBm"
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class DeviceViewHolder(itemView: View, listener: OnItemClickListener): RecyclerView.ViewHolder(itemView) {
        val deviceName: TextView = itemView.findViewById(R.id.device_name)
        val macAddress: TextView = itemView.findViewById(R.id.mac_address)
        val signalStrength: TextView = itemView.findViewById(R.id.signal_strength)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        override fun toString(): String {
            return super.toString() + " '" + macAddress.text + "'"
        }
    }
}