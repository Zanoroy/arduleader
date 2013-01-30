package com.geeksville.andropilot

import com.geeksville.gmaps._
import org.mavlink.messages.ardupilotmega.msg_mission_item
import com.ridemission.scandroid.AndroidLogger
import com.google.android.gms.maps.model._
import com.geeksville.flight.VehicleMonitor
import org.mavlink.messages.ardupilotmega.msg_mission_ack

class WaypointMarker(val msg: msg_mission_item) extends SmartMarker with AndroidLogger {
  def lat = msg.x
  def lon = msg.y
  override def title = Some("Waypoint #" + msg.seq)
  override def snippet = Some(msg.toString)

  override def icon: Option[BitmapDescriptor] = {
    if (isHome) // Show a house for home
      Some(BitmapDescriptorFactory.fromResource(R.drawable.lz_blue))
    else msg.current match {
      case 2 => // Guided
        Some(BitmapDescriptorFactory.fromResource(R.drawable.flag))
      case _ =>
        None // Default
    }
  }

  /// The magic home position
  def isHome = (msg.current != 2) && (msg.seq == 0)

  /// If the airplane is heading here
  def isCurrent = msg.current == 1

  override def toString = title.get
}

/**
 * A draggable marker that will send movement commands to the vehicle
 */
class DraggableWaypointMarker(val v: VehicleMonitor, msg: msg_mission_item) extends WaypointMarker(msg) {

  override def lat_=(n: Double) { msg.x = n.toFloat }
  override def lon_=(n: Double) { msg.y = n.toFloat }

  override def draggable = !isHome

  override def onDragEnd() {
    super.onDragEnd()
    debug("Drag ended on " + this)

    val r = msg
    v.sendWithRetry(r, classOf[msg_mission_ack])
  }
}