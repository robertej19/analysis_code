#!/usr/bin/groovy

import java.io.*
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.Date
import org.jlab.clas.physics.LorentzVector
import org.jlab.clas.physics.Vector3
import org.jlab.detector.base.DetectorType
import org.jlab.groot.base.GStyle
import org.jlab.groot.data.GraphErrors
import org.jlab.groot.data.H1F
import org.jlab.groot.data.H2F
import org.jlab.groot.data.TDirectory
import org.jlab.groot.fitter.DataFitter
import org.jlab.groot.graphics.EmbeddedCanvas
import org.jlab.groot.group.DataGroup
import org.jlab.groot.math.F1D
import org.jlab.io.base.DataBank
import org.jlab.io.base.DataEvent
import org.jlab.io.hipo.HipoDataSource
import org.jlab.io.hipo.HipoDataSync
import groovy.io.FileType

MyMods.enable() //I don't know what this does, its from Andrey, don't touch it, it works

println("\n \n \n \n \n \n \n \n \n \n \n \n \n \n")

Mil = 1000000


def ele = LorentzVector.withPID(11,3,4,5)
def pro = LorentzVector.withPID(2212,2,3,7)
def ele_out = LorentzVector.withPID(11,3,1,2)
def pro_out = LorentzVector.withPID(2212,3,5,12)


def evec = new Vector3()
evec.setMagThetaPhi(ele.p(), ele.theta(), ele.phi())

def vLept = ele.vect().cross(ele_out.vect())
def vHad = pro.vect().cross(pro_out.vect())

def shouldBeNull = vLept.dot(ele.vect())

def PlaneDot = vLept.dot(vHad)
def cosangle = PlaneDot/vLept.mag()/vHad.mag()
def angle = Math.toDegrees( Math.acos(cosangle))

println(vLept)
println(vHad)

println(angle)

