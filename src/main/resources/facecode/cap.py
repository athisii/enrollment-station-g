import cv2
import numpy as np
from PIL import Image
import pybase64 as base64
import glob

def converttobase64()
    path = '/usr/share/enrollment/croppedimg/'
    dest = '/usr/share/enrollment/croppedimg/'
    for filepath in glob.glob(path+'*png'):

        file_name = filepath[filepath.index(path)+len(path):] 
        with open(filepath, "rb") as image2string:
            converted_string = base64.b64encode(image2string.read())
        print(converted_string)
    
        with open(dest+file_name[:-3]+'csv', "wb") as file:
            file.write(converted_string)

frame = cv2.imread('/usr/share/enrollment/images/input.jpg')
frame = cv2.flip(frame, 1)
emptyframe=frame[np.abs(100):np.abs(450), np.abs(int(150)):np.abs(450)]
#dim = (413, 413)	
#resized = cv2.resize(emptyframe, dim, interpolation = cv2.INTER_AREA)
#cv2.imwrite('/usr/share/enrollment/croppedimg/out.jpg', resized)
#rcompressed = cv2.resize(emptyframe, (192, 192) , interpolation = cv2.INTER_AREA)
#cv2.imwrite("/usr/share/enrollment/croppedimg/compressed.jpg",rcompressed)
image = Image.fromarray(cv2.cvtColor(face_, cv2.COLOR_BGR2RGB)).convert("RGBA")
image.resize((113,113), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/out.png', "PNG", dpi=(300, 300), optimize=True,quality=95)
image = Image.fromarray(cv2.cvtColor(face_, cv2.COLOR_BGR2RGB)).convert("RGBA")
image.resize((64,64), Image.ANTIALIAS).save('/usr/share/enrollment/croppedimg/compressed.png', "PNG", dpi=(300, 300), optimize=True,quality=95)
converttobase64()

print("Valid image cropped along Red Box since face is not detected. Press start capture, if face not correct")
