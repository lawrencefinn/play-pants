package playpants.tool

import java.io.File

import play.router.RoutesCompiler
import play.router.RoutesCompiler.RoutesCompilationError

object RoutesGen {
  case class Params(sources: Seq[File] = Seq.empty,
                    routesImports: Seq[String] = Seq.empty,
                    generateReverseRouter: Boolean = true,
                    generateForwardRouter: Boolean = true,
                    namespaceReverseRouter: Boolean = false,
                    target: File = new File("."))

  def main(args: Array[String]): Unit = {
    val parser = new scopt.OptionParser[Params]("routes-gen") {
      head("routes-gen", "1.0")
      opt[Seq[File]]("sources") valueName "<input1,input2>" action {
        (sources, c) => c.copy(sources = sources)
      } text "comma separated list of route files"

      opt[Seq[String]]("routes_imports") valueName "<import1,import2>" action {
        (routes, c) => c.copy(routesImports = routes)
      } text "Comma separated list of imports for the router."

      opt[Boolean]("generate_reverse_router") valueName "<value>" action {
        (v, c) => c.copy(generateReverseRouter = v)
      } text "Whether the reverse router should be generated. Setting to false may reduce compile times if it's not needed"

      opt[Boolean]("generate_forward_router") valueName "<value>" action {
        (v, c) => c.copy(generateForwardRouter = v)
      } text "Whether the forward router should be generated."

      opt[Boolean]("namespace_reverse_router") valueName "<value>" action {
        (v, c) => c.copy(namespaceReverseRouter = v)
      } text "Whether the reverse router should be namespaced. Useful if you have many routers that use the same actions."

      opt[File]("target") required() valueName "<target_dir>" action {
        (target, c) => c.copy(target = target)
      } text "directory to write generated sources"
    }

    parser.parse(args, Params()) match {
      case None => System.exit(1)
      case Some(params) =>
        val errors = params.sources.flatMap { source =>
          try {
            RoutesCompiler.compile(source, params.target, params.routesImports,
              params.generateReverseRouter, true, params.namespaceReverseRouter)
            None
          } catch {
            case e: RoutesCompilationError => Some(e)
          }
        }
        val msgs = errors.map { error =>
            val linePart = error.line.map(t => s":$t").getOrElse("")
            s"${error.source}$linePart: ${error.message}"
        }

        if (errors.nonEmpty) {
          System.err.println(errors.mkString("\n\n"))
          System.exit(1)
        }
    }
  }
}
