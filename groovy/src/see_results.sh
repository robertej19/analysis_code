
#../scripts/this_run_groovy_4_args.sh main_ana_multi.groovy /mnt/d/CLAS12Data/skim8-20200629 $1 $2 $3

TXT=$1"txt"
HIPO=$1"hipo"
echo $HIPO
/home/bobby/bin/wsl-open.sh  $TXT
../scripts/view_hipo.sh $HIPO 

