/*
 * Copyright 2018-2019 scala-steward contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalasteward.core.util

import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import org.http4s.Method.{GET, POST}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.Client
import org.http4s.{Method, Request, Uri}

final class HttpJsonClient[F[_]: Sync](
    client: Client[F],
    modify: Request[F] => F[Request[F]]
) {
  def get[A: Decoder](uri: Uri): F[A] =
    request[A](GET, uri)

  def post[A: Decoder](uri: Uri): F[A] =
    request[A](POST, uri)

  def postWithBody[A: Decoder, B: Encoder](uri: Uri, body: B): F[A] =
    expectJsonOf[A](Request[F](POST, uri).withEntity(body)(jsonEncoderOf[F, B]))

  def request[A: Decoder](method: Method, uri: Uri): F[A] =
    expectJsonOf[A](Request[F](method, uri))

  def expectJsonOf[A: Decoder](req: Request[F]): F[A] =
    client.expect[A](modify(req))(jsonOf)
}
