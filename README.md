# Twitter word source

Gets words from Twitter and makes them available via an API, all the while recording some metrics on the rendered words.

# API

## GET /api/words

Returns a word.

## GET /api/top{N}words

*N* the number of top words

Returns the most requested words.

## GET /api/top{N}letters

*N* the number of top letters

Returns the most requested letters (as part of the word).

# How the Twitter bit works

An Akka actor checks the queue (an abstraction based on Redis) every few seconds and if the size is below a threshold, it opens the tap to the Twitter firehose, and vice versa for it having enough words.
