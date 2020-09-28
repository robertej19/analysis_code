
#Usage: ./runit.sh 0 1 3 - the first number is the number of events in each file to process (0 means run all 
#events in each file)
# - the second number is the number of files in the folder to process. processes alphabetically. 1 processes 
# just the first file
# the third number is how many cores you want to use e.g. 3 will use 3 cores
# in this script below, the analysis code must point to a directory, not a specific file

#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /lustre19/expphy/volatile/clas12/rg-a/production/pass0/physTrain/dst/train/skim8/ $1 $2

#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /work/clas12/rg-a/trains/v16_v2/skim8_ep/ $1 $2

#for testing on ifarm
#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /u/home/robertej/sample-data/  $1 $2

#for testing on local
#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/good-data-20200629 $1 $2

../scripts/this_run_groovy_4_args.sh main_ana_multi.groovy /mnt/d/CLAS12Data/skim4/skim4-20200927 $1 $2 $3

#../scripts/view_hipo.sh output_file_histos.hipo

