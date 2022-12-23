import cv2
import numpy as np
frame = cv2.imread('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/images/input.jpg')
frame = cv2.flip(frame, 1)
emptyframe=frame[np.abs(100):np.abs(450), np.abs(int(150)):np.abs(450)]
dim = (413, 413)	
resized = cv2.resize(emptyframe, dim, interpolation = cv2.INTER_AREA)
cv2.imwrite('/home/boss/NetBeansProjects/src/main/resources/haar_facedetection/croppedimg/out.jpg', resized)
print("Valid image cropped along Red Box since face is not detected. Press start capture, if face not correct")
