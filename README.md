# GazePlay Voice Synthesis

This project allows a user to generate natural-sounding voices using the Amazon Polly service. You will need an AWS
account to run this project yourself.

## Set up

You will need to set up your AWS account on your local machine. Instructions on how to do this can be found 
[in Amazon's documentation](https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/setup-credentials.html).

Additionally, you will need to create a unique bucket to store the generated files. You will use this as a command line
argument to run the program.

Additionally, you should fill the [inputs.csv](src/main/resources/inputs.csv) file with the translations you want to
synthesize. You will have to use the AWS region codes as column headers for different languages.
 
__Warning: In the program's current state, adding new languages will require code changes.__

## Running the program

Run the program on the command line, assuming the bucket name is "gazeplay-voice-bucket"
```
> gradlew run --args gazeplay-voice-bucket      # Windows
$ ./gradlew run --args gazeplay-voice-bucket    # Unix
```

Be patient. The synthesis runs asynchronously in the cloud but the program will wait for each one to complete before 
moving onto the next. This is to ensure rate limiting, and that you don't incur high cloud usage fees.

## Retrieving the sounds

The sounds will reside in the root of your chosen bucket. They will have the correct GazePlay compatible name, except 
for a unique identifier at the end of the filename. You should download all the files after they have been generated
and remove this identifier. They will then be ready for inclusion in GazePlay.
