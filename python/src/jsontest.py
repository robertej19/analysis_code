
import json

with open('../../histogram_dict.json') as f:
  data = json.load(f)

"""
# Output: {'name': 'Bob', 'languages': ['English', 'Fench']}
for hist in data:
    print("hist is")
    print(hist)
    for key in data[hist]:
        for i in key:
            print("key is:")
            print(i)
            print(key[i])
"""
for hist in data:
    print("Histogram root name: {}".format(hist))
    for key in data[hist][0]:
        print("hist property {} has value {}".format(key,data[hist][0][key]))