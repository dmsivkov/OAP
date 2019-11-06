#include "codegen/expr_visitor.h"

#include <arrow/status.h>

arrow::Status ExprVisitor::Visit(const gandiva::FieldNode& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::FunctionNode& node) {
  auto desc = node.descriptor();
  if (std::find(gdv.begin(), gdv.end(), desc->name()) != gdv.end()) {
    // gandiva can handle this
    codegen_type = GANDIVA;
  } else if (std::find(ce.begin(), ce.end(), desc->name()) != ce.end()) {
    // we need to implement our own function to handle this
    codegen_type = COMPUTE_EXT;
  } else {
    // arrow compute can handle this
    codegen_type = ARROW_COMPUTE;
  }
  return arrow::Status::OK();
}

arrow::Status ExprVisitor::Visit(const gandiva::IfNode& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::LiteralNode& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::BooleanNode& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::InExpressionNode<int32_t>& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::InExpressionNode<int64_t>& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}

arrow::Status ExprVisitor::Visit(const gandiva::InExpressionNode<std::string>& node) {
  arrow::Status status = arrow::Status::OK();
  return status;
}
