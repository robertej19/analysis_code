#!/usr/bin/python

import uproot
from icecream import ic
import numpy as np
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import subprocess
import os
import time
import shutil
from shutil import copyfile


ic.disable()

#filename = "converted_filtered_skim8_005032.root"
#filename = "converted_filtered_processed.root"
#ff = ROOT.TFile(sys.argv[1])
#ff = ROOT.TFile(filename)




"""
tree.keys()
['nmb', 'Pp', 'Ppx', 'Ppy', 'Ppz', 'Ptheta', 'Pphi', 
'Pvx', 'Pvy', 'Pvz', 'Pvt', 'PSector', 'Pbeta', 'Pstat', 
'nml', 'Ep', 'Epx', 'Epy', 'Epz', 'Etheta', 'Ephi', 'Evx', 
'Evy', 'Evz', 'Evt', 'Ebeta', 'Estat', 'ESector', 'nmg', 
'Gp', 'Gpx', 'Gpy', 'Gpz', 'Gtheta', 'Gphi', 'Gvx', 'Gvy', 
'Gvz', 'Gvt', 'GSector', 'Gbeta', 'Gstat', 'beamQ', 
'liveTime', 'startTime', 'RFTime', 'helicity', 
'helicityRaw', 'EventNum', 'RunNum', 'Q2', 'Nu', 'q', 'qx',
'qy', 'qz', 'W2', 'xB', 't', 'combint', 'mPpx', 'mPpy',
'mPpz', 'mPp', 'mmP', 'meP', 'Mpx', 'Mpy', 'Mpz', 'Mp',
'mm', 'me', 'mGpx', 'mGpy', 'mGpz', 'mGp', 'mmG', 'meG',
'Pi0p', 'Pi0px', 'Pi0py', 'Pi0pz', 'Pi0theta',
'Pi0phi', 'Pi0M', 'Pi0Sector', 'pIndex', 'gIndex1',
'gIndex2', 'trento', 'trento2', 'trento3']  

"""



data_dir = "op_dir/DVEP_roots/"
output_dir = "op_dir/final_txts/"
data_list = os.listdir(data_dir)


#root_macro = "scriptPi0_new.C"

#default_root_outname = "output_root_file.root"
#default_root_inname = "input_root_file.root"

#infile = "testerfile.txt"
#outfile = data_dir+"fixed.txt"


#new_list = data_list[1:3]
#print(new_list)

for count,filename in enumerate(data_list):
    print("on file {} out of {}, named {}".format(count,len(data_list),filename))

    #filename = "skim8_005036_filtered_DVEP.root"

    output_file_ending = filename.replace(".root",".txt")
    file = uproot.open(data_dir+filename)
    output_file = open(output_dir+output_file_ending,"w")


    tree = file["T"]


    q2 = tree["Q2"].array()
    xB = tree["xB"].array()
    t_mom = tree["t"].array()
    trent1 = tree["trento"].array()
    #trent2 = tree["trento2"].array()
    #trent3 = tree["trento3"].array()
    #pi0M = tree['Pi0M'].array()

    
    #filt_pi = []
    #filt_trent = []
    #filtering
    for count,item in enumerate(q2):
    #    print(item[0])
    #    filt_trent.append(item[0])
    #    filt_pi.append(pi0M[count][0])
        output_file.write("{},{},{},{}\n".format(q2[count],xB[count],t_mom[count][0],trent1[count][0]))

    print("done filtering")


    #arr = np.array(filt_trent)

    print("number of events is: {}".format(len(q2)))



#i = 0
#while i < 100:
    #print(trent1[i])
    #print(trent2[i])
    #print(trent3[i])
#    print(pi0M[i])
#    i +=1

#plt.hist(filt_pi,60)
#plt.show()
