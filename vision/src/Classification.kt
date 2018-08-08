// Copyright 2018 Google Inc.
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
package com.google.kotlinvision

import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.extensions.*
import com.google.protobuf.ByteString

import java.io.IOException
import java.io.File

fun main(args: Array<String>) {
    val requests = ArrayList<AnnotateImageRequest>()
    val imageFile = if (args.isEmpty()) {
        "./resources/doggo.jpg" // Image file path
    } else {
        args[0] // grab args[0] for file
    }

    val file = File(imageFile)
    if (!file.exists()) {
        throw NoSuchFileException(file = file, reason = "The file you specified does not exist")
    }

    try {
        val vision = ImageAnnotatorClient.create() // Create an Image Annotator

        // create a image using a typesafe DSL
        val img = image { content = ByteString.copyFrom(file.readBytes()) }
        val request = annotateImageRequest {
            // Add a feature and supply an unnamed argument
            feature(Feature.Type.LABEL_DETECTION)
            // Add a feature and supply two named arguments
            feature(type = Feature.Type.LANDMARK_DETECTION, maxresults = 2)
            // Add a feature using a typesafe DSL
            feature {
                type = Feature.Type.FACE_DETECTION
                maxResults = 2
            }
            // add an image directly to the request as a property
            image = img
        }

        requests.add(request)

        // Performs label detection on the image file
        val response = vision.batchAnnotateImages(requests)
        val responses = response.responsesList

        for (resp in responses) {
            if (resp.hasError()) {
                println("Error: ${resp.error.message}")
                return
            }

            for (annotation in resp.labelAnnotationsList) {
                for ((k, v) in annotation.allFields) {
                    println("${k.name}: $v")
                }
                println()
            }
        }
    } catch (e: IOException) {
        println("Image annotation failed:")
        println(e.message)
    }
}
