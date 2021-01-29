import subprocess
import os
import time

jobs_dir = "jsub_factory/"

jobs_list = os.listdir(jobs_dir)

print(jobs_list)

for count,file in enumerate(jobs_list):
    print("Trying to submit job {}".format(count))
    process = subprocess.Popen(['jsub', file],
                     stdout=subprocess.PIPE, 
                     stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    print("STDOUT is {}".format(stdout))
   # print("STDERR is {}".format(stderr))
    time.sleep(1)