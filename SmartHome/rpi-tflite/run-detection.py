import sys
import numpy as np
import tensorflow as tf
import cv2
from PIL import Image

if len(sys.argv) != 2:
    print("Usage: python script.py <image_file>")
    sys.exit(1)

image_path = sys.argv[1]
img = tf.keras.utils.load_img(
    image_path, target_size=(224, 224)
)
img_array = tf.keras.utils.img_to_array(img)
img_array = tf.expand_dims(img_array, 0)

interpreter = tf.lite.Interpreter(model_path='tf_lite_model.tflite')

print(interpreter.get_signature_list())


classify_lite = interpreter.get_signature_runner('serving_default')
classify_lite

predictions_lite = classify_lite(input_1=img_array)['dense_3']
score_lite = tf.nn.softmax(predictions_lite)

class_labels = ['cat', 'dog', 'person']
predicted_class = class_labels[np.argmax(score_lite)]
print(score_lite)
print(f'Predicted class: {predicted_class}')