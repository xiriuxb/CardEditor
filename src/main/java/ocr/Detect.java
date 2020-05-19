/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ocr;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateFileResponse;
import com.google.cloud.vision.v1.AnnotateFileResponse.Builder;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.AsyncAnnotateFileRequest;
import com.google.cloud.vision.v1.AsyncAnnotateFileResponse;
import com.google.cloud.vision.v1.AsyncBatchAnnotateFilesResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Block;
import com.google.cloud.vision.v1.ColorInfo;
import com.google.cloud.vision.v1.CropHint;
import com.google.cloud.vision.v1.CropHintsAnnotation;
import com.google.cloud.vision.v1.DominantColorsAnnotation;
import com.google.cloud.vision.v1.EntityAnnotation;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.GcsDestination;
import com.google.cloud.vision.v1.GcsSource;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.InputConfig;
import com.google.cloud.vision.v1.LocalizedObjectAnnotation;
import com.google.cloud.vision.v1.LocationInfo;
import com.google.cloud.vision.v1.OperationMetadata;
import com.google.cloud.vision.v1.OutputConfig;
import com.google.cloud.vision.v1.Page;
import com.google.cloud.vision.v1.Paragraph;
import com.google.cloud.vision.v1.SafeSearchAnnotation;
import com.google.cloud.vision.v1.Symbol;
import com.google.cloud.vision.v1.TextAnnotation;
import com.google.cloud.vision.v1.WebDetection;
import com.google.cloud.vision.v1.WebDetection.WebEntity;
import com.google.cloud.vision.v1.WebDetection.WebImage;
import com.google.cloud.vision.v1.WebDetection.WebLabel;
import com.google.cloud.vision.v1.WebDetection.WebPage;
import com.google.cloud.vision.v1.WebDetectionParams;
import com.google.cloud.vision.v1.Word;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Detect {

  /**
   * Detects entities, sentiment, and syntax in a document using the Vision API.
   *
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  public static void main(String[] args) throws Exception, IOException {
    argsHelper(args, System.out);
  }

  /**
   * Helper that handles the input passed to the program.
   *
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  public static void argsHelper(String[] args, PrintStream out) throws Exception, IOException {
    if (args.length < 1) {
      out.println("Usage:");
      out.printf(
          "\tmvn exec:java -DDetect -Dexec.args=\"<command> <path-to-image>\"\n"
              + "\tmvn exec:java -DDetect -Dexec.args=\"ocr <path-to-file> <path-to-destination>\""
              + "\n"
              + "Commands:\n"
              + "\tfaces | labels | landmarks | logos | text | safe-search | properties"
              + "| web | web-entities | web-entities-include-geo | crop | ocr \n"
              + "| object-localization \n"
              + "Path:\n\tA file path (ex: ./resources/wakeupcat.jpg) or a URI for a Cloud Storage "
              + "resource (gs://...)\n"
              + "Path to File:\n\tA path to the remote file on Cloud Storage (gs://...)\n"
              + "Path to Destination\n\tA path to the remote destination on Cloud Storage for the"
              + " file to be saved. (gs://BUCKET_NAME/PREFIX/)\n");
      detectText("C:\\Users\\HP\\Downloads\\foolish.jpeg", out);
      return;
    }
    String command = args[0];
    String path = args.length > 1 ? args[1] : "";

    if (command.equals("text")) {
      if (path.startsWith("gs://")) {
        detectTextGcs(path, out);
      } else {
        detectText(path, out);
      }
    }  else if (command.equals("fulltext")) {
      if (path.startsWith("gs://")) {
        detectDocumentTextGcs(path, out);
      } else {
        detectDocumentText(path, out);
      }
    }
  }


  /**
   * Detects text in the specified image.
   *
   * @param filePath The path to the file to detect text in.
   * @param out A {@link PrintStream} to write the detected text to.
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  // [START vision_text_detection]
  public static void detectText(String filePath, PrintStream out) throws Exception, IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          out.printf("Error: %s\n", res.getError().getMessage());
          return;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
          out.printf("Text: %s\n", annotation.getDescription());
          out.printf("Position : %s\n", annotation.getBoundingPoly());
        }
      }
    }
  }
  // [END vision_text_detection]

  /**
   * Detects text in the specified remote image on Google Cloud Storage.
   *
   * @param gcsPath The path to the remote file on Google Cloud Storage to detect text in.
   * @param out A {@link PrintStream} to write the detected text to.
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  // [START vision_text_detection_gcs]
  public static void detectTextGcs(String gcsPath, PrintStream out) throws Exception, IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          out.printf("Error: %s\n", res.getError().getMessage());
          return;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        for (EntityAnnotation annotation : res.getTextAnnotationsList()) {
          out.printf("Text: %s\n", annotation.getDescription());
          out.printf("Position : %s\n", annotation.getBoundingPoly());
        }
      }
    }
  }
  // [END vision_text_detection_gcs]

 
  /**
   * Detects image properties such as color frequency from the specified remote image on Google
   * Cloud Storage.
   *
   * @param gcsPath The path to the remote file on Google Cloud Storage to detect properties on.
   * @param out A {@link PrintStream} to write
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  // [START vision_image_property_detection_gcs]
  public static void detectPropertiesGcs(String gcsPath, PrintStream out)
      throws Exception, IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Type.IMAGE_PROPERTIES).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          out.printf("Error: %s\n", res.getError().getMessage());
          return;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        DominantColorsAnnotation colors = res.getImagePropertiesAnnotation().getDominantColors();
        for (ColorInfo color : colors.getColorsList()) {
          out.printf(
              "fraction: %f\nr: %f, g: %f, b: %f\n",
              color.getPixelFraction(),
              color.getColor().getRed(),
              color.getColor().getGreen(),
              color.getColor().getBlue());
        }
      }
    }
  }
  // [END vision_image_property_detection_gcs]

   
  /**
   * Performs document text detection on a local image file.
   *
   * @param filePath The path to the local file to detect document text on.
   * @param out A {@link PrintStream} to write the results to.
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  // [START vision_fulltext_detection]
  public static void detectDocumentText(String filePath, PrintStream out)
      throws Exception, IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      client.close();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          out.printf("Error: %s\n", res.getError().getMessage());
          return;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        TextAnnotation annotation = res.getFullTextAnnotation();
        for (Page page : annotation.getPagesList()) {
          String pageText = "";
          for (Block block : page.getBlocksList()) {
            String blockText = "";
            for (Paragraph para : block.getParagraphsList()) {
              String paraText = "";
              for (Word word : para.getWordsList()) {
                String wordText = "";
                for (Symbol symbol : word.getSymbolsList()) {
                  wordText = wordText + symbol.getText();
                  out.format(
                      "Symbol text: %s (confidence: %f)\n",
                      symbol.getText(), symbol.getConfidence());
                }
                out.format("Word text: %s (confidence: %f)\n\n", wordText, word.getConfidence());
                paraText = String.format("%s %s", paraText, wordText);
              }
              // Output Example using Paragraph:
              out.println("\nParagraph: \n" + paraText);
              out.format("Paragraph Confidence: %f\n", para.getConfidence());
              blockText = blockText + paraText;
            }
            pageText = pageText + blockText;
          }
        }
        out.println("\nComplete annotation:");
        out.println(annotation.getText());
      }
    }
  }
  // [END vision_fulltext_detection]

  /**
   * Performs document text detection on a remote image on Google Cloud Storage.
   *
   * @param gcsPath The path to the remote file on Google Cloud Storage to detect document text on.
   * @param out A {@link PrintStream} to write the results to.
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  // [START vision_fulltext_detection_gcs]
  public static void detectDocumentTextGcs(String gcsPath, PrintStream out)
      throws Exception, IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ImageSource imgSource = ImageSource.newBuilder().setGcsImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feat = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();
    AnnotateImageRequest request =
        AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      client.close();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          out.printf("Error: %s\n", res.getError().getMessage());
          return;
        }
        // For full list of available annotations, see http://g.co/cloud/vision/docs
        TextAnnotation annotation = res.getFullTextAnnotation();
        for (Page page : annotation.getPagesList()) {
          String pageText = "";
          for (Block block : page.getBlocksList()) {
            String blockText = "";
            for (Paragraph para : block.getParagraphsList()) {
              String paraText = "";
              for (Word word : para.getWordsList()) {
                String wordText = "";
                for (Symbol symbol : word.getSymbolsList()) {
                  wordText = wordText + symbol.getText();
                  out.format(
                      "Symbol text: %s (confidence: %f)\n",
                      symbol.getText(), symbol.getConfidence());
                }
                out.format("Word text: %s (confidence: %f)\n\n", wordText, word.getConfidence());
                paraText = String.format("%s %s", paraText, wordText);
              }
              // Output Example using Paragraph:
              out.println("\nParagraph: \n" + paraText);
              out.format("Paragraph Confidence: %f\n", para.getConfidence());
              blockText = blockText + paraText;
            }
            pageText = pageText + blockText;
          }
        }
        out.println("\nComplete annotation:");
        out.println(annotation.getText());
      }
    }
  }
  // [END vision_fulltext_detection_gcs]

 

 

  
}