
#Usage: ./runit.sh 0 1 - the first number is the number of events in each file to process (0 means run all 
#events in each file)
# - the second number is the number of files in the folder to process. processes alphabetically. 1 processes 
# just the first file
# in this script below, the analysis code must point to a directory, not a specific file

#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /lustre19/expphy/volatile/clas12/rg-a/production/pass0/physTrain/dst/train/skim8/ $1 $2

#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /work/clas12/rg-a/trains/v16_v2/skim8_ep/ $1 $2

#for testing on ifarm
#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /u/home/robertej/sample-data/  $1 $2

#for testing on local
#../scripts/this_run_groovy_4_args.sh prime_sector.groovy /mnt/c/Users/rober/Dropbox/Bobby/Linux/work/CLAS12/mit-clas12-analysis/good-data-20200629 $1 $2

../scripts/this_run_groovy_4_args.sh main_ana.groovy /mnt/d/CLAS12Data/skim8-20200629 $1 $2

#../scripts/view_hipo.sh output_file_histos.hipo

