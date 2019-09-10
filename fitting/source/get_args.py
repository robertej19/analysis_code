#****************************************************************
"""
# File Description
"""
#****************************************************************
import argparse

def get_args():
  argparser = argparse.ArgumentParser()
  """
  argparser.add_argument('-b','--UserSubmissionID', default='none', help = 'Enter the ID# of the batch you want to submit (e.g. -b 23)')
  argparser.add_argument('-t','--test', help = 'Use this flag (no arguments) if you are NOT on a farm node and want to test the submission flag (-s)', action = 'store_true')
  argparser.add_argument('-s','--submit', help = 'Use this flag (no arguments) if you want to submit the job', action = 'store_true')
  argparser.add_argument('-w','--write_files', help = 'Use this flag (no arguments) if you want submission files to be written out to text files', action = 'store_true')
  argparser.add_argument('-y','--scard_type', default='0', help = 'Enter scard type (e.g. -y 1 for submitting type 1 scards)')
  argparser.add_argument(fs.debug_short,fs.debug_longdash, default = fs.debug_default,help = fs.debug_help)
  argparser.add_argument('-l','--lite',help = "use -l or --lite to connect to sqlite DB, otherwise use MySQL DB", action = 'store_true')
  argparser.add_argument('-o','--OutputDir', default='none', help = 'Enter full path of your desired output directory, e.g. /u/home/robertej')
"""
  argparser.add_argument('filename',help = 'Name of file to analyze',nargs='?',)
  argparser.add_argument('-p','--plot_type', default='0', help = 'Enter help message here')
  args = argparser.parse_args()

  return args
